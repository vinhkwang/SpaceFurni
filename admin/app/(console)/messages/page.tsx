import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";

function ChatBubbleIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-6 w-6 stroke-current" fill="none" strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 5h16v11H8l-4 4z" />
    </svg>
  );
}

export default async function MessagesPage() {
  const dictionary = getDictionary(await getLocale());

  return (
    <div className="flex flex-col items-center justify-center gap-4 rounded-2xl border border-hairline-soft bg-white px-6.5 py-24 text-center">
      <span className="flex h-12 w-12 items-center justify-center rounded-pill bg-surface-raised text-ink-muted">
        <ChatBubbleIcon />
      </span>
      <div className="text-[14px] font-semibold text-ink">{dictionary.messages.liveChatNotPartOfBuild}</div>
      <p className="max-w-[360px] text-[12.5px] leading-relaxed text-ink-muted">{dictionary.messages.liveChatBody}</p>
    </div>
  );
}
