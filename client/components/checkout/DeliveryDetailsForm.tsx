"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/Input";
import { formatMoney } from "@/lib/formatting/formatMoney";
import { formatDeliveryDate } from "@/lib/formatting/formatDeliveryDate";
import type { DeliveryWindow } from "@/lib/api/types";

type DeliveryDetailsFormValues = {
  fullName: string;
  phone: string;
  street: string;
  district: string;
  city: string;
  note: string;
  deliveryWindow: DeliveryWindow;
};

type DeliveryDetailsFieldErrors = Partial<Record<keyof Omit<DeliveryDetailsFormValues, "note" | "deliveryWindow">, string>>;

const STORAGE_KEY = "spacefurni:checkout:deliveryDetails";

const NEXT_DAY_DELIVERY_FEE_AMOUNT = 300_000;

const VIETNAMESE_PHONE_PATTERN = /^(0|\+84)(3|5|7|8|9)[0-9]{8}$/;

const EMPTY_FORM_VALUES: DeliveryDetailsFormValues = {
  fullName: "",
  phone: "",
  street: "",
  district: "",
  city: "",
  note: "",
  deliveryWindow: "STANDARD",
};

function loadStoredFormValues(): DeliveryDetailsFormValues | null {
  try {
    const storedJson = sessionStorage.getItem(STORAGE_KEY);
    if (storedJson === null) {
      return null;
    }
    return { ...EMPTY_FORM_VALUES, ...(JSON.parse(storedJson) as Partial<DeliveryDetailsFormValues>) };
  } catch {
    return null;
  }
}

function validateFormValues(formValues: DeliveryDetailsFormValues): DeliveryDetailsFieldErrors {
  const fieldErrors: DeliveryDetailsFieldErrors = {};

  if (formValues.fullName.trim() === "") {
    fieldErrors.fullName = "Enter the recipient's full name";
  }
  if (!VIETNAMESE_PHONE_PATTERN.test(formValues.phone.trim())) {
    fieldErrors.phone = "Enter a valid Vietnamese phone number";
  }
  if (formValues.street.trim() === "") {
    fieldErrors.street = "Enter a street address";
  }
  if (formValues.district.trim() === "") {
    fieldErrors.district = "Enter a district";
  }
  if (formValues.city.trim() === "") {
    fieldErrors.city = "Enter a city";
  }

  return fieldErrors;
}

export function DeliveryDetailsForm() {
  const router = useRouter();
  const [formValues, setFormValues] = useState<DeliveryDetailsFormValues>(
    () => loadStoredFormValues() ?? EMPTY_FORM_VALUES,
  );
  const [fieldErrors, setFieldErrors] = useState<DeliveryDetailsFieldErrors>({});

  function updateField<FieldName extends keyof DeliveryDetailsFormValues>(
    fieldName: FieldName,
    fieldValue: DeliveryDetailsFormValues[FieldName],
  ): void {
    setFormValues((previousFormValues) => ({ ...previousFormValues, [fieldName]: fieldValue }));
  }

  function submitDeliveryDetails(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault();
    const validationErrors = validateFormValues(formValues);
    setFieldErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(formValues));
    } catch {}
    router.push("/checkout?step=payment");
  }

  const standardDeliveryDateLabel = formatDeliveryDate(new Date());

  return (
    <form
      onSubmit={submitDeliveryDetails}
      className="rounded-2xl border border-hairline bg-white px-8.5 py-8"
    >
      <h2 className="mb-6 text-[19px] font-medium">Delivery details</h2>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input
          label="Full name"
          placeholder="Nguyen Minh"
          value={formValues.fullName}
          onChange={(event) => updateField("fullName", event.target.value)}
          errorMessage={fieldErrors.fullName}
          autoComplete="name"
        />
        <Input
          label="Phone"
          placeholder="+84 …"
          value={formValues.phone}
          onChange={(event) => updateField("phone", event.target.value)}
          errorMessage={fieldErrors.phone}
          autoComplete="tel"
        />
        <div className="sm:col-span-2">
          <Input
            label="Street address"
            placeholder="House number, street"
            value={formValues.street}
            onChange={(event) => updateField("street", event.target.value)}
            errorMessage={fieldErrors.street}
            autoComplete="address-line1"
          />
        </div>
        <Input
          label="District"
          placeholder="Thanh Xuan"
          value={formValues.district}
          onChange={(event) => updateField("district", event.target.value)}
          errorMessage={fieldErrors.district}
          autoComplete="address-level2"
        />
        <Input
          label="City"
          placeholder="Ha Noi"
          value={formValues.city}
          onChange={(event) => updateField("city", event.target.value)}
          errorMessage={fieldErrors.city}
          autoComplete="address-level1"
        />
        <div className="sm:col-span-2">
          <label className="flex flex-col gap-2">
            <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
              Delivery note <span className="normal-case tracking-normal text-ink-muted/70">(optional)</span>
            </span>
            <textarea
              value={formValues.note}
              onChange={(event) => updateField("note", event.target.value)}
              placeholder="Lift access, best time to arrive…"
              rows={3}
              className="resize-none rounded-xl border border-hairline bg-canvas px-4 py-3.5 text-[13px] text-ink outline-none transition-colors duration-200 placeholder:text-ink-muted focus:border-terracotta"
            />
          </label>
        </div>
      </div>

      <div className="mt-6.5 flex flex-col gap-3 border-t border-hairline pt-6">
        <span className="text-[10.5px] uppercase tracking-[0.14em] text-ink-muted">
          Delivery window
        </span>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <label
            className={`flex cursor-pointer items-center gap-3 rounded-xl border px-4.5 py-4 transition-colors duration-200 ${
              formValues.deliveryWindow === "STANDARD"
                ? "border-deep bg-surface"
                : "border-hairline hover:border-deep/40"
            }`}
          >
            <input
              type="radio"
              name="deliveryWindow"
              value="STANDARD"
              checked={formValues.deliveryWindow === "STANDARD"}
              onChange={() => updateField("deliveryWindow", "STANDARD")}
              className="sr-only"
            />
            <span
              className={`h-4 w-4 shrink-0 rounded-full border ${
                formValues.deliveryWindow === "STANDARD" ? "border-[5px] border-deep" : "border-hairline"
              }`}
            />
            <span>
              <span className="block text-[12.5px] font-semibold">Standard · free</span>
              <span className="mt-0.5 block text-[11px] text-ink-muted">
                {standardDeliveryDateLabel}, 9:00–18:00
              </span>
            </span>
          </label>

          <label
            className={`flex cursor-pointer items-center gap-3 rounded-xl border px-4.5 py-4 transition-colors duration-200 ${
              formValues.deliveryWindow === "NEXT_DAY"
                ? "border-deep bg-surface"
                : "border-hairline hover:border-deep/40"
            }`}
          >
            <input
              type="radio"
              name="deliveryWindow"
              value="NEXT_DAY"
              checked={formValues.deliveryWindow === "NEXT_DAY"}
              onChange={() => updateField("deliveryWindow", "NEXT_DAY")}
              className="sr-only"
            />
            <span
              className={`h-4 w-4 shrink-0 rounded-full border ${
                formValues.deliveryWindow === "NEXT_DAY" ? "border-[5px] border-deep" : "border-hairline"
              }`}
            />
            <span>
              <span className="block text-[12.5px] font-semibold">
                Next day · {formatMoney(NEXT_DAY_DELIVERY_FEE_AMOUNT)}
              </span>
              <span className="mt-0.5 block text-[11px] text-ink-muted">Order before 15:00 today</span>
            </span>
          </label>
        </div>
      </div>

      <button
        type="submit"
        className="mt-6.5 flex h-[54px] w-full cursor-pointer items-center justify-center gap-3 rounded-pill bg-deep text-[11.5px] font-semibold uppercase tracking-[0.14em] text-white transition-colors duration-300 hover:bg-terracotta"
      >
        Continue to payment
      </button>
    </form>
  );
}
