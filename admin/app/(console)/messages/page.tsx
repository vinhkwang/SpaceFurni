function ChatBubbleIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-6 w-6 stroke-current" fill="none" strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 5h16v11H8l-4 4z" />
    </svg>
  );
}

export default function MessagesPage() {
  return (
    <div className="flex flex-col items-center justify-center gap-4 rounded-2xl border border-hairline-soft bg-white px-6.5 py-24 text-center">
      <span className="flex h-12 w-12 items-center justify-center rounded-pill bg-surface-raised text-ink-muted">
        <ChatBubbleIcon />
      </span>
      <div className="text-[14px] font-semibold text-ink">Live chat is not part of this build</div>
      <p className="max-w-[360px] text-[12.5px] leading-relaxed text-ink-muted">
        Customer messaging would require a live chat channel, which is out of scope here. Reach customers directly
        by phone or email using the details on their order.
      </p>
    </div>
  );
}
