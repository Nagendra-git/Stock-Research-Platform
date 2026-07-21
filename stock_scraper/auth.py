"""
auth.py
-------
Handles authentication against Screener.in using Playwright's Sync API.

Exposes a single reusable function:
    login(page, email, password)

The function navigates to the login page, fills in credentials, submits the
form, and waits (using explicit/expected conditions rather than sleep) until
the login has actually succeeded -- verified by checking that the browser
has navigated away from the login page and that a "logout"/account element
is visible in the page header.
"""

from playwright.sync_api import Page, TimeoutError as PlaywrightTimeoutError

LOGIN_URL = "https://www.screener.in/login/"


def login(page: Page, email: str, password: str, timeout: int = 30000) -> None:
    """
    Log into Screener.in.

    Args:
        page: An active Playwright Page object.
        email: Screener.in account email.
        password: Screener.in account password.
        timeout: Max wait time (ms) for each explicit wait condition.

    Raises:
        RuntimeError: If login fails for any reason (bad credentials,
                      network issue, unexpected page structure, etc).
    """
    try:
        print(f"[auth] Navigating to login page: {LOGIN_URL}")
        page.goto(LOGIN_URL, wait_until="domcontentloaded", timeout=timeout)

        # Wait explicitly for the login form fields to be visible/ready.
        print("[auth] Waiting for login form to be visible...")
        email_input = page.locator("input#id_username")
        password_input = page.locator("input#id_password")

        email_input.wait_for(state="visible", timeout=timeout)
        password_input.wait_for(state="visible", timeout=timeout)

        print("[auth] Filling in credentials...")
        email_input.fill(email)
        password_input.fill(password)

        # Locate the submit button (Screener's login form submit button).
        submit_button = page.locator("button[type='submit']")
        submit_button.wait_for(state="visible", timeout=timeout)

        print("[auth] Submitting login form...")
        with page.expect_navigation(wait_until="domcontentloaded", timeout=timeout):
            submit_button.click()

        # Explicit wait: confirm login succeeded by waiting for an
        # authenticated-only element (account/logout link) OR confirming
        # we are no longer on the /login/ URL.
        print("[auth] Verifying login success...")
        try:
            page.wait_for_url(lambda url: "/login" not in url, timeout=timeout)
        except PlaywrightTimeoutError:
            pass  # Fall through to secondary check below.

        # Secondary / more robust check: look for a known post-login element.
        # Screener shows an "Account" / user menu link once logged in.
        account_indicator = page.locator("a[href='/logout/'], a[href*='/user/']")
        try:
            account_indicator.first.wait_for(state="attached", timeout=timeout)
        except PlaywrightTimeoutError:
            # If neither the URL changed nor the account indicator appeared,
            # treat this as a failed login (e.g. wrong credentials -> error
            # message shown, form stays on /login/).
            error_text = ""
            try:
                error_text = page.locator(".alert, .error, .help-block").first.inner_text(timeout=2000)
            except Exception:
                pass
            raise RuntimeError(
                "Login verification failed - still appears to be on login page. "
                f"Possible error message: '{error_text}'"
            )

        print("[auth] Login successful.")

    except PlaywrightTimeoutError as e:
        raise RuntimeError(f"Timed out during login process: {e}") from e
    except Exception as e:
        raise RuntimeError(f"Login failed: {e}") from e
