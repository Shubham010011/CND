import { useEffect, useState } from "react";
import { getMarkets } from "../api/client";

const REFRESH_MS = 30000; // stay well under CoinGecko's free-tier rate limit

export default function Market() {
  const [coins, setCoins] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const fetchMarkets = async () => {
    try {
      const { data } = await getMarkets("usd", 25, 1);
      setCoins(data);
      setError("");
    } catch {
      setError("Could not load live market data right now.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMarkets();
    const interval = setInterval(fetchMarkets, REFRESH_MS);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="card">
      <h2>Live Market</h2>
      <p className="muted">Prices refresh every 30s (CoinGecko).</p>
      {loading && <p>Loading prices...</p>}
      {error && <p className="error-text">{error}</p>}
      {coins.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th>#</th>
              <th>Coin</th>
              <th>Price (USD)</th>
              <th>24h</th>
              <th>Market Cap</th>
            </tr>
          </thead>
          <tbody>
            {coins.map((c) => (
              <tr key={c.id}>
                <td>{c.market_cap_rank}</td>
                <td>
                  <img src={c.image} alt={c.symbol} width={18} height={18} className="coin-icon" />
                  {c.name} <span className="muted">({c.symbol.toUpperCase()})</span>
                </td>
                <td>${c.current_price?.toLocaleString()}</td>
                <td className={c.price_change_percentage_24h >= 0 ? "positive" : "negative"}>
                  {c.price_change_percentage_24h?.toFixed(2)}%
                </td>
                <td>${c.market_cap?.toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
