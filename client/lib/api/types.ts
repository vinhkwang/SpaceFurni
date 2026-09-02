export type UserRole = "CUSTOMER" | "ADMIN";

export type OrderStatus = "PENDING" | "PAID" | "PACKING" | "DELIVERED" | "CANCELLED";

export type DeliveryWindow = "STANDARD" | "NEXT_DAY";

export type PaymentMethod = "CARD" | "CASH_ON_DELIVERY" | "BANK_TRANSFER";

export type PaymentStatus = "PENDING" | "AUTHORISED" | "CAPTURED" | "FAILED";

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type AuthenticationResponse = {
  accessToken: string;
  refreshToken: string;
};

export type CurrentUserResponse = {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
};

export type CategoryTreeResponse = {
  id: string;
  name: string;
  slug: string;
  imageUrl: string | null;
  productCount: number;
  subCategories: CategoryTreeResponse[];
};

export type ProductBadgeResponse = {
  label: string;
  variant: "SALE" | "NEW" | "BESTSELLER";
};

export type ProductSummaryResponse = {
  id: string;
  sku: string;
  slug: string;
  name: string;
  categoryName: string;
  priceAmount: number;
  compareAtPriceAmount: number | null;
  currencyCode: string;
  ratingAverage: number | null;
  reviewCount: number;
  primaryImageUrl: string | null;
  badge: ProductBadgeResponse | null;
};

export type ProductSpecificationEntry = {
  key: string;
  value: string;
};

export type ProductDetailResponse = {
  id: string;
  sku: string;
  slug: string;
  name: string;
  categoryName: string;
  priceAmount: number;
  compareAtPriceAmount: number | null;
  currencyCode: string;
  ratingAverage: number | null;
  reviewCount: number;
  shortDescription: string;
  longDescription: string;
  dimensions: string;
  material: string;
  primaryColorName: string;
  badge: ProductBadgeResponse | null;
  imageUrls: string[];
  specifications: ProductSpecificationEntry[];
  colorSwatchHexCodes: string[];
  availableQuantity: number;
  stockLabel: string;
  relatedProducts: ProductSummaryResponse[];
};

export type CartLineResponse = {
  productId: string;
  productSlug: string;
  productName: string;
  imageUrl: string | null;
  unitPriceAmount: number;
  currencyCode: string;
  quantity: number;
  lineTotalAmount: number;
};

export type PriceBreakdownResponse = {
  subtotalAmount: number;
  shippingAmount: number;
  discountAmount: number;
  totalAmount: number;
  currencyCode: string;
  appliedPromotionCode: string | null;
  amountToFreeShippingAmount: number;
};

export type CartResponse = {
  id: string | null;
  guestToken: string | null;
  lines: CartLineResponse[];
  priceBreakdown: PriceBreakdownResponse;
};

export type OrderDeliveryDetailsResponse = {
  fullName: string;
  phone: string;
  street: string;
  district: string;
  city: string;
  note: string | null;
};

export type OrderItemResponse = {
  productId: string;
  productName: string;
  sku: string;
  unitPriceAmount: number;
  quantity: number;
  lineTotalAmount: number;
};

export type OrderResponse = {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  subtotalAmount: number;
  shippingAmount: number;
  discountAmount: number;
  totalAmount: number;
  currencyCode: string;
  promotionCode: string | null;
  deliveryDetails: OrderDeliveryDetailsResponse;
  deliveryWindow: DeliveryWindow;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  placedAt: string;
  items: OrderItemResponse[];
};

export type OrderSummaryResponse = {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  totalAmount: number;
  currencyCode: string;
  itemCount: number;
  placedAt: string;
};
