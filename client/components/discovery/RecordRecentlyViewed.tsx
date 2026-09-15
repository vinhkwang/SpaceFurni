"use client";

import { useEffect } from "react";
import { recordRecentlyViewedAction } from "@/lib/discovery/recentlyViewedActions";

type RecordRecentlyViewedProps = {
  productId: string;
};

export function RecordRecentlyViewed({ productId }: RecordRecentlyViewedProps) {
  useEffect(() => {
    void recordRecentlyViewedAction(productId);
  }, [productId]);

  return null;
}
