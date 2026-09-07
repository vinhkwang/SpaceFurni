import { DepartmentGrid } from "@/components/home/DepartmentGrid";
import { HeroCarousel } from "@/components/home/HeroCarousel";
import { HeroPromoBanners } from "@/components/home/HeroPromoBanners";
import { ProductRail } from "@/components/home/ProductRail";
import { ServicePromiseStrip } from "@/components/home/ServicePromiseStrip";
import { WeeklyDropBand } from "@/components/home/WeeklyDropBand";
import { WorkshopBand } from "@/components/home/WorkshopBand";
import { Container } from "@/components/ui/Container";

export default function HomePage() {
  return (
    <main className="pb-22">
      <Container alignToNavLabel className="grid grid-cols-[1fr_486px] gap-3.5 pt-5.5">
        <HeroCarousel />
        <HeroPromoBanners />
      </Container>
      <Container alignToNavLabel className="mt-14">
        <ServicePromiseStrip />
      </Container>
      <Container alignToNavLabel className="mt-21">
        <DepartmentGrid />
      </Container>
      <Container alignToNavLabel className="mt-21">
        <ProductRail
          eyebrow="Just landed"
          title="New arrivals"
          sort="newest"
          shopAllHref="/products"
        />
      </Container>
      <Container alignToNavLabel className="mt-21">
        <WeeklyDropBand />
      </Container>
      <Container alignToNavLabel className="mt-21">
        <ProductRail
          eyebrow="Loved by 1,200 homes"
          title="Our bestsellers"
          sort="rating"
          shopAllHref="/products"
        />
      </Container>
      <Container alignToNavLabel className="mt-22">
        <WorkshopBand />
      </Container>
    </main>
  );
}
