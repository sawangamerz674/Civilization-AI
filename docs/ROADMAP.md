# Roadmap

- [x] **Phase 1** — Project setup, architecture, registries, Gradle,
      configuration, logging, save system, networking.
- [x] **Phase 2** — Core civilization data model, memory system, villager
      profiles, and persistence.
- [x] **Phase 3** — AI framework, task scheduler, job assignment, needs
      evaluation.
- [x] **Phase 4** — Resource gathering, storage, logistics, economy.
- [x] **Phase 5** — Building planner, terrain analysis, road generation,
      construction.
- [x] **Phase 6** — Population growth, relationships, reputation, events,
      technology, defense.
- [x] **Phase 7** — Optimization, multiplayer synchronization, commands,
      API, documentation, profiling, final polishing.

All seven phases are complete. Civilization AI is a working, end-to-end
Minecraft Forge mod: real Villager entities are detected and organized
into autonomous civilizations that hold jobs, gather and trade resources,
construct buildings and roads, grow their population, form relationships,
earn or lose player reputation, weather dynamic incidents, advance through
a technology tree, and respond to real threats — all through commands,
a documented config, a public API for other mods, and a test suite proving
the core simulation is usable without a running game.

See `docs/ARCHITECTURE.md` for the full per-phase design record, including
every deliberate scope cut and why it was made.
