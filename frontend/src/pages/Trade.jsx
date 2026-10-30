import { useEffect, useMemo, useState } from "react";
import { getQuote, getSupportedCoins, buyCoin, sellCoin, swapCoins, getMyWallet } from "../api/client";
import { shareText } from "../utils/share";

const TABS = ["buy", "sell", "swap"];

export default function Trade() {
  const [tab, setTab] = useState("buy");
  const [coins, setCoins] = useState([]);
  const [wallet, setWallet] = useState(null);

  const [buyCoinSymbol, setBuyCoinSymbol] = useState("BTC");
  const [buyUsdt, setBuyUsdt] = useState("");
  const [buyQuote, setBuyQuote] = useState(null);

  const [sellCoinSymbol, setSellCoinSymbol] = useState("BTC");
  const [sellAmount, setSellAmount] = useState("");
  const [sellQuote, setSellQuote] = useState(null);

  const [swapFrom, setSwapFrom] = useState("BTC");
  const [swapTo, setSwapTo] = useState("ETH");
  const [swapAmount, setSwapAmount] = useState("");

  const [status, setStatus] = useState(null);
  const [lastTrade, setLastTrade] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    getSupportedCoins().then(({ data }) => setCoins(data.filter((c) => c !== "USDT")));
    refreshWallet();
  }, []);

  const refreshWallet = () => {
    getMyWallet().then(({ data }) => setWallet(data)).catch(() => {});
  };

  const balanceOf = (symbol) =>
    wallet?.balances.find((b) => b.coinSymbol === symbol)?.amount ?? 0;

  // Live quote for buy tab
  useEffect(() => {
    if (tab !== "buy") return;
    getQuote(buyCoinSymbol).then(({ data }) => setBuyQuote(data.priceUsd)).catch(() => setBuyQuote(null));
  }, [tab, buyCoinSymbol]);

  // Live quote for sell tab
  useEffect(() => {
    if (tab !== "sell") return;
    getQuote(sellCoinSymbol).then(({ data }) => setSellQuote(data.priceUsd)).catch(() => setSellQuote(null));
  }, [tab, sellCoinSymbol]);

  const estimatedBuyAmount = useMemo(() => {
    if (!buyQuote || !buyUsdt) return null;
    return (parseFloat(buyUsdt) / buyQuote).toFixed(8);
  }, [buyQuote, buyUsdt]);

  const estimatedSellProceeds = useMemo(() => {
    if (!sellQuote || !sellAmount) return null;
    return (parseFloat(sellAmount) * sellQuote).toFixed(2);
  }, [sellQuote, sellAmount]);

  const handleBuy = async (e) => {
    e.preventDefault();
    setStatus(null);
    setLoading(true);
    try {
      const { data } = await buyCoin({ coinSymbol: buyCoinSymbol, usdtAmount: parseFloat(buyUsdt) });
      onTradeSuccess(data);
      setBuyUsdt("");
    } catch (err) {
      setStatus({ type: "error", message: err.response?.data?.message || "Buy failed." });
    } finally {
      setLoading(false);
    }
  };

  const handleSell = async (e) => {
    e.preventDefault();
    setStatus(null);
    setLoading(true);
    try {
      const { data } = await sellCoin({ coinSymbol: sellCoinSymbol, coinAmount: parseFloat(sellAmount) });
      onTradeSuccess(data);
      setSellAmount("");
    } catch (err) {
      setStatus({ type: "error", message: err.response?.data?.message || "Sell failed." });
    } finally {
      setLoading(false);
    }
  };

  const handleSwap = async (e) => {
    e.preventDefault();
    setStatus(null);
    setLoading(true);
    try {
      const { data } = await swapCoins({
        fromCoinSymbol: swapFrom,
        toCoinSymbol: swapTo,
        amount: parseFloat(swapAmount),
      });
      onTradeSuccess(data);
      setSwapAmount("");
    } catch (err) {
      setStatus({ type: "error", message: err.response?.data?.message || "Swap failed." });
    } finally {
      setLoading(false);
    }
  };

  const onTradeSuccess = (data) => {
    setLastTrade(data);
    setStatus({ type: "success", message: data.shareText });
    refreshWallet();
  };

  const handleShare = async () => {
    if (!lastTrade) return;
    const result = await shareText(lastTrade.shareText);
    if (result.method === "clipboard") {
      setStatus({ type: "success", message: "Copied to clipboard: " + lastTrade.shareText });
    }
  };

  return (
    <div className="card">
      <h2>Trade</h2>

      <div className="tab-row">
        {TABS.map((t) => (
          <button
            key={t}
            type="button"
            className={`tab-btn ${tab === t ? "tab-btn-active" : ""}`}
            onClick={() => {
              setTab(t);
              setStatus(null);
            }}
          >
            {t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {tab === "buy" && (
        <form onSubmit={handleBuy}>
          <label>
            Coin to buy
            <select value={buyCoinSymbol} onChange={(e) => setBuyCoinSymbol(e.target.value)}>
              {coins.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>
          <p className="muted">
            Live price: {buyQuote ? `$${buyQuote.toLocaleString()}` : "loading..."} · Your USDT
            balance: {balanceOf("USDT")}
          </p>
          <label>
            USDT to spend
            <input
              type="number"
              step="any"
              min="0"
              value={buyUsdt}
              onChange={(e) => setBuyUsdt(e.target.value)}
              required
            />
          </label>
          {estimatedBuyAmount && (
            <p className="muted">You'll receive approximately {estimatedBuyAmount} {buyCoinSymbol}</p>
          )}
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? "Buying..." : `Buy ${buyCoinSymbol}`}
          </button>
        </form>
      )}

      {tab === "sell" && (
        <form onSubmit={handleSell}>
          <label>
            Coin to sell
            <select value={sellCoinSymbol} onChange={(e) => setSellCoinSymbol(e.target.value)}>
              {coins.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>
          <p className="muted">
            Live price: {sellQuote ? `$${sellQuote.toLocaleString()}` : "loading..."} · Your{" "}
            {sellCoinSymbol} balance: {balanceOf(sellCoinSymbol)}
          </p>
          <label>
            Amount to sell
            <input
              type="number"
              step="any"
              min="0"
              value={sellAmount}
              onChange={(e) => setSellAmount(e.target.value)}
              required
            />
          </label>
          {estimatedSellProceeds && (
            <p className="muted">You'll receive approximately {estimatedSellProceeds} USDT</p>
          )}
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? "Selling..." : `Sell ${sellCoinSymbol}`}
          </button>
        </form>
      )}

      {tab === "swap" && (
        <form onSubmit={handleSwap}>
          <label>
            From
            <select value={swapFrom} onChange={(e) => setSwapFrom(e.target.value)}>
              {coins.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>
          <p className="muted">Your {swapFrom} balance: {balanceOf(swapFrom)}</p>
          <label>
            To
            <select value={swapTo} onChange={(e) => setSwapTo(e.target.value)}>
              {coins.filter((c) => c !== swapFrom).map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>
          <label>
            Amount of {swapFrom} to swap
            <input
              type="number"
              step="any"
              min="0"
              value={swapAmount}
              onChange={(e) => setSwapAmount(e.target.value)}
              required
            />
          </label>
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? "Swapping..." : `Swap ${swapFrom} \u2192 ${swapTo}`}
          </button>
        </form>
      )}

      {status && (
        <div className={status.type === "error" ? "error-text" : "success-text"} style={{ marginTop: 14 }}>
          {status.message}
        </div>
      )}

      {lastTrade && (
        <button type="button" className="btn btn-secondary" style={{ marginTop: 10 }} onClick={handleShare}>
          Share this trade
        </button>
      )}
    </div>
  );
}
