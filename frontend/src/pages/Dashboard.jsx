import { useEffect, useState } from "react";
import { getMyWallet, getMyQrCode } from "../api/client";
import { shareText } from "../utils/share";

export default function Dashboard() {
  const [wallet, setWallet] = useState(null);
  const [qrCode, setQrCode] = useState(null);
  const [showQr, setShowQr] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);
  const [shareStatus, setShareStatus] = useState("");

  useEffect(() => {
    let mounted = true;
    getMyWallet()
      .then(({ data }) => mounted && setWallet(data))
      .catch(() => mounted && setError("Could not load your wallet."));
    return () => {
      mounted = false;
    };
  }, []);

  const handleShowQr = async () => {
    if (qrCode) {
      setShowQr((s) => !s);
      return;
    }
    try {
      const { data } = await getMyQrCode();
      setQrCode(data.qrCodeBase64Png);
      setShowQr(true);
    } catch {
      setError("Could not generate QR code.");
    }
  };

  const handleCopy = () => {
    if (!wallet) return;
    navigator.clipboard.writeText(wallet.walletId);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  const handleSharePortfolio = async () => {
    if (!wallet) return;
    const holdings = wallet.balances
      .filter((b) => parseFloat(b.amount) > 0)
      .map((b) => `${b.amount} ${b.coinSymbol}`)
      .join(", ");
    const text = holdings
      ? `My CryptoX demo portfolio: ${holdings} \uD83D\uDCC8`
      : "Just started trading on CryptoX \uD83D\uDE80";
    const result = await shareText(text);
    if (result.method === "clipboard") {
      setShareStatus("Copied to clipboard!");
      setTimeout(() => setShareStatus(""), 2000);
    }
  };

  if (error) return <p className="error-text">{error}</p>;
  if (!wallet) return <p>Loading wallet...</p>;

  return (
    <div className="card">
      <h2>Your Wallet</h2>

      <div className="wallet-id-row">
        <code className="wallet-id">{wallet.walletId}</code>
        <button className="btn btn-small" onClick={handleCopy}>
          {copied ? "Copied!" : "Copy"}
        </button>
        <button className="btn btn-small" onClick={handleShowQr}>
          {showQr ? "Hide QR" : "Receive (QR)"}
        </button>
        <button className="btn btn-small" onClick={handleSharePortfolio}>
          Share portfolio
        </button>
      </div>
      {shareStatus && <p className="success-text">{shareStatus}</p>}

      {showQr && qrCode && (
        <div className="qr-box">
          <img
            src={`data:image/png;base64,${qrCode}`}
            alt="Wallet QR code"
            width={220}
            height={220}
          />
          <p className="muted">Others scan this to send you crypto.</p>
        </div>
      )}

      <h3>Balances</h3>
      {wallet.balances.length === 0 ? (
        <p className="muted">No balances yet.</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Coin</th>
              <th>Amount</th>
            </tr>
          </thead>
          <tbody>
            {wallet.balances.map((b) => (
              <tr key={b.coinSymbol}>
                <td>{b.coinSymbol}</td>
                <td>{b.amount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
