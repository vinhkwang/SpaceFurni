import type { Dictionary } from "@/lib/i18n/getDictionary";

export function localizedCategoryName(
  dictionary: Dictionary,
  category: { slug: string; name: string },
): string {
  return dictionary.catalog.categoryNames[category.slug] ?? category.name;
}

function slugifyCategoryName(categoryName: string): string {
  return categoryName
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

export function localizedCategoryLabel(dictionary: Dictionary, categoryName: string): string {
  return dictionary.catalog.categoryNames[slugifyCategoryName(categoryName)] ?? categoryName;
}
