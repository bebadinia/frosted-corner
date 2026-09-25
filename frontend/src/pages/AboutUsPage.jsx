export function AboutUsPage() {
  return (
    <div className="mx-auto w-full max-w-5xl px-6 py-16">
      <div className="mb-3 text-xs font-bold uppercase tracking-[0.2em] text-primary">About Us</div>
      <h1 className="font-serif text-4xl font-bold">About Frosted Corner</h1>
      <p className="mt-6 max-w-3xl text-base leading-8 text-muted-foreground">
        This page is ready for the Frosted Corner story, company background, franchise information,
        mission, and any other content you want included in the demo.
      </p>
      <div className="mt-10 rounded-2xl border border-border bg-white p-8 shadow-sm">
        <h2 className="font-serif text-2xl font-bold">Content placeholder</h2>
        <p className="mt-3 text-sm leading-7 text-muted-foreground">
          Send the final About Us content when you are ready and this section can be replaced without changing the page route or navigation.
        </p>
      </div>
    </div>
  );
}
