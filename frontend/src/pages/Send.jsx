import { useEffect, useRef, useState } from "react";
import { Html5Qrcode } from "html5-qrcode";
import { sendCrypto, lookupWallet } from "../api/client";

const SCANNER_ELEMENT_ID = "qr-scanner-region";

export default function Send() {
  const [toWalletId, setToWalletId] = useState("");
  const [coinSymbol, setCoinSymbol] = useState("USDT");
  const [amount, setAmount] = useState("");
  const [note, setNote] = useState("");
  const [scanning, setScanning] = useState(false);
  const [status, setStatus] = useState(null); // { type: 'success'|'error', message }
  const [loading, setLoading] = useState(false);

  const scannerRef = useRef(null);

  useEffect(() => {
    // Clean up the camera stream if the user navigates away mid-scan.
    return () => {
      if (scannerRef.current) {
        scannerRef.current.stop().catch(() => {});
      }
    };
  }, []);

  const startScan = async () => {
    setStatus(null);
    setScanning(true);
    try {
      const html5Qrcode = new Html5Qrcode(SCANNER_ELEMENT_ID);
      scannerRef.current = html5Qrcode;

      await html5Qrcode.start(
        { facingMode: "environment" },
        { fps: 10, qrbox: 250 },
        (decodedText) => {
          setToWalletId(decodedText.trim());
          stopScan();
        },
        () => {
          /* per-frame decode failures are normal while aiming the camera; ignore */
        }
      );
    } catch (err) {
      setStatus({ type: "error", message: "Camera access failed: " + err.message });
      setScanning(false);
    }
  };

  const stopScan = async () => {
    if (scannerRef.current) {
      try {
        await scannerRef.current.stop();
        scannerRef.current.clear();
      } catch {
        /* already stopped */
      }
    }
    setScanning(false);
  };

  const handleVerify = async () => {
    if (!toWalletId) return;
    try {
      await lookupWallet(toWalletId.trim());
      setStatus({ type: "success", message: "Wallet address is valid." });
    } catch {
      setStatus({ type: "error", message: "No wallet found with that address." });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatus(null);
    setLoading(true);
    try {
      const { data } = await sendCrypto({
        toWalletId: toWalletId.trim(),
        coinSymbol: coinSymbol.trim().toUpperCase(),
        amount: parseFloat(amount),
        note: note || undefined,
      });
      setStatus({
        type: "success",
        message: `Sent ${data.amount} ${data.coinSymbol} to ${data.toWalletId.slice(0, 10)}...`,
      });
      setToWalletId("");
      setAmount("");
      setNote("");
    } catch (err) {
      setStatus({ type: "error", message: err.response?.data?.message || "Transfer failed." });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>Send Crypto</h2>

      <div className="scan-section">
        {!scanning ? (
          <button type="button" className="btn btn-secondary" onClick={startScan}>
            Scan recipient QR code
          </button>
        ) : (
          <button type="button" className="btn btn-secondary" onClick={stopScan}>
            Stop scanning
          </button>
        )}
        <div id={SCANNER_ELEMENT_ID} className="qr-scanner-region" />
      </div>

      <form onSubmit={handleSubmit}>
        <label>
          Recipient wallet ID
          <div className="input-with-action">
            <input
              value={toWalletId}
              onChange={(e) => setToWalletId(e.target.value)}
              placeholder="0x..."
              required
            />
            <button type="button" className="btn btn-small" onClick={handleVerify}>
              Verify
            </button>
          </div>
        </label>

        <label>
          Coin
          <select value={coinSymbol} onChange={(e) => setCoinSymbol(e.target.value)}>
            <option value="USDT">USDT</option>
            <option value="BTC">BTC</option>
            <option value="ETH">ETH</option>
            <option value="BNB">BNB</option>
            <option value="SOL">SOL</option>
          </select>
        </label>

        <label>
          Amount
          <input
            type="number"
            step="any"
            min="0"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />
        </label>

        <label>
          Note (optional)
          <input value={note} onChange={(e) => setNote(e.target.value)} />
        </label>

        {status && (
          <p className={status.type === "error" ? "error-text" : "success-text"}>{status.message}</p>
        )}

        <button className="btn btn-primary" type="submit" disabled={loading}>
          {loading ? "Sending..." : "Send"}
        </button>
      </form>
    </div>
  );
}
