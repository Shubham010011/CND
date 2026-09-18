import { useEffect, useState } from "react";
import { getHistory } from "../api/client";
import { useAuth } from "../context/AuthContext";

const TRADE_TYPES = new Set(["BUY", "SELL", "SWAP"]);

function describeRow(tx, walletId) {
  if (tx.type === "BUY") {
    return { direction: "Buy", detail: `${tx.amount} ${tx.coinSymbol} \u2192 ${tx.counterAmount} ${tx.counterCoinSymbol}` };
  }
  if (tx.type === "SELL") {
    return { direction: "Sell", detail: `${tx.amount} ${tx.coinSymbol} \u2192 ${tx.counterAmount} ${tx.counterCoinSymbol}` };
  }
  if (tx.type === "SWAP") {
    return { direction: "Swap", detail: `${tx.amount} ${tx.coinSymbol} \u2192 ${tx.counterAmount} ${tx.counterCoinSymbol}` };
  }
  const isOutgoing = tx.fromWalletId === walletId;
  const counterparty = isOutgoing ? tx.toWalletId : tx.fromWalletId;
  return {
    direction: isOutgoing ? "Sent" : "Received",
    detail: `${tx.amount} ${tx.coinSymbol} \u2194 ${counterparty.slice(0, 12)}...`,
  };
}

export default function History() {
  const [transactions, setTransactions] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const { walletId } = useAuth();

  useEffect(() => {
    getHistory()
      .then(({ data }) => setTransactions(data))
      .catch(() => setError("Could not load transaction history."))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading history...</p>;
  if (error) return <p className="error-text">{error}</p>;

  return (
    <div className="card">
      <h2>Transaction History</h2>
      {transactions.length === 0 ? (
        <p className="muted">No transactions yet.</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Type</th>
              <th>Details</th>
              <th>Status</th>
              <th>Time</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((tx) => {
              const { direction, detail } = describeRow(tx, walletId);
              const isTrade = TRADE_TYPES.has(tx.type);
              const isOutgoing = !isTrade && tx.fromWalletId === walletId;
              return (
                <tr key={tx.id}>
                  <td className={isTrade ? "direction-trade" : isOutgoing ? "direction-out" : "direction-in"}>
                    {direction}
                  </td>
                  <td>{detail}</td>
                  <td>{tx.status}</td>
                  <td>{new Date(tx.timestamp).toLocaleString()}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
}
