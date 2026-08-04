import "../../styles/WeeklyTrend.css";
import { WeeklyMomentum } from "../../types/WeeklyMomentum";

interface Props {
    data: WeeklyMomentum[];
    sortBy: string;
    direction: string;
    onSort: (field: string) => void;
}

export default function WeeklyTrendTable({
    data,
    sortBy,
    direction,
    onSort,
}: Props) {

    const getArrow = (field: string) => {
        if (sortBy !== field) return "";
        return direction === "asc" ? " ▲" : " ▼";
    };

    return (
        <div className="weekly-container">

            <div className="table-wrapper">
                <table className="weekly-table">

                    <thead className="bg-indigo-600 text-white">
                        <tr>
                            <th>Symbol</th>

                            <th
                                style={{ cursor: "pointer" }}
                                onClick={() => onSort("swingScore")}
                            >
                                Swing{getArrow("swingScore")}
                            </th>

                            <th
                                style={{ cursor: "pointer" }}
                                onClick={() => onSort("buyerSellerScore")}
                            >
                                Weekly BS{getArrow("buyerSellerScore")}
                            </th>

                            <th
                                style={{ cursor: "pointer" }}
                                onClick={() => onSort("monthlyBuyerSellerScore")}
                            >
                                Monthly BS{getArrow("monthlyBuyerSellerScore")}
                            </th>

                            <th
                                style={{ cursor: "pointer" }}
                                onClick={() => onSort("threeMonthBuyerSellerScore")}
                            >
                                3M BS{getArrow("threeMonthBuyerSellerScore")}
                            </th>

                            <th
                                style={{ cursor: "pointer" }}
                                onClick={() => onSort("sixMonthBuyerSellerScore")}
                            >
                                6M BS{getArrow("sixMonthBuyerSellerScore")}
                            </th>

                            <th
                                style={{ cursor: "pointer" }}
                                onClick={() => onSort("pastThreeDaysPricePercentage")}
                            >
                                Past 3 Days %{getArrow("pastThreeDaysPricePercentage")}
                            </th>
                        </tr>
                    </thead>

                    <tbody>
                        {data.map((item) => (
                            <tr key={item.id}>
                                <td>{item.symbol}</td>

                                <td>{item.swingScore.toFixed(2)}</td>

                                <td
                                    className={
                                        item.buyerSellerScore >= 0
                                            ? "positive"
                                            : "negative"
                                    }
                                >
                                    {item.buyerSellerScore.toFixed(2)}%
                                </td>

                                <td
                                    className={
                                        item.monthlyBuyerSellerScore >= 0
                                            ? "positive"
                                            : "negative"
                                    }
                                >
                                    {item.monthlyBuyerSellerScore.toFixed(2)}%
                                </td>

                                <td
                                    className={
                                        item.threeMonthBuyerSellerScore >= 0
                                            ? "positive"
                                            : "negative"
                                    }
                                >
                                    {item.threeMonthBuyerSellerScore.toFixed(2)}%
                                </td>

                                <td
                                    className={
                                        item.sixMonthBuyerSellerScore >= 0
                                            ? "positive"
                                            : "negative"
                                    }
                                >
                                    {item.sixMonthBuyerSellerScore.toFixed(2)}%
                                </td>

                                <td
                                    className={
                                        item.pastThreeDaysPricePercentage >= 0
                                            ? "positive"
                                            : "negative"
                                    }
                                >
                                    {item.pastThreeDaysPricePercentage.toFixed(2)}%
                                </td>
                            </tr>
                        ))}
                    </tbody>

                </table>
            </div>
        </div>
    );
}