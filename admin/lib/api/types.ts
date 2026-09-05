export type UserRole = "CUSTOMER" | "ADMIN";

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

export type ProductStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";

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

export type AdminSummaryResponse = {
  publishedProductCount: number;
  ordersTodayCount: number;
  pendingOrdersCount: number;
  lowStockProductCount: number;
};

export type CategoryTreeResponse = {
  id: string;
  name: string;
  slug: string;
  imageUrl: string | null;
  productCount: number;
  subCategories: CategoryTreeResponse[];
};

export type AdminProductDetailResponse = {
  id: string;
  title: string;
  departmentSlug: string;
  subCategorySlug: string;
  price: number;
  stock: number;
  shortDescription: string | null;
  longDescription: string | null;
  dimensions: string | null;
  material: string | null;
  primaryColorName: string | null;
  imageUrl: string | null;
  status: ProductStatus;
  version: number;
};

export type AdminProductRowResponse = {
  id: string;
  imageUrl: string;
  title: string;
  sku: string;
  categoryLabel: string;
  priceAmount: number;
  currencyCode: string;
  stockOnHand: number;
  status: ProductStatus;
};

export type AdminOrderRowResponse = {
  orderNumber: string;
  customerName: string;
  district: string;
  itemSummary: string;
  lineCount: number;
  paymentLabel: string;
  placedAt: string;
  totalAmount: number;
  currencyCode: string;
  status: OrderStatus;
};

export type AdminOrderListResponse = {
  orders: PageResponse<AdminOrderRowResponse>;
  statusCounts: Partial<Record<OrderStatus, number>>;
};

export type OrderTimelineStepResponse = {
  label: string;
  detail: string;
  complete: boolean;
};

export type AdminOrderCustomerResponse = {
  fullName: string;
  email: string;
  phone: string;
};

export type AdminOrderDeliveryAddressResponse = {
  street: string;
  district: string;
  city: string;
  note: string | null;
};

export type AdminOrderLineResponse = {
  productName: string;
  unitPriceAmount: number;
  quantity: number;
  lineTotalAmount: number;
};

export type AdminOrderDetailResponse = {
  orderNumber: string;
  status: OrderStatus;
  customer: AdminOrderCustomerResponse;
  deliveryAddress: AdminOrderDeliveryAddressResponse;
  deliveryWindow: DeliveryWindow;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  subtotalAmount: number;
  shippingAmount: number;
  discountAmount: number;
  totalAmount: number;
  currencyCode: string;
  placedAt: string;
  lines: AdminOrderLineResponse[];
  timeline: OrderTimelineStepResponse[];
};
