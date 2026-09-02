import { HeroCarousel } from "@/components/home/HeroCarousel";
import { Container } from "@/components/ui/Container";

export default function HomePage() {
  return (
    <main>
      <Container className="pt-5.5">
        <HeroCarousel />
      </Container>
    </main>
  );
}
