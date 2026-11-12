# The Pokemon Trading Card Game Randomizer
A randomizer in the works for the GBC Pokemon Trading Card Game. No official release yet as I'm still setting up infrastructure to support all the planned features to make things go smoother in the future.

## Backstory
I recently got into randomizers for Pokemon mainline games. I've always loved the PTCG on GBC so I looked for a randomizer for it but didn't find any with a  wide breadth of options so I decided to tackle the challenge myself.

Its a project of love for me so I'm not sure when updates and fixes will come out.

## Command line

The runnable JAR entry point is `RandomizerLaunch`. Useful flags:

- `--headless` with `--config path/to/ptcgr_randomize.yaml` runs a batch randomization without the GUI.
- `--rom path.gbc` selects the input ROM (GUI or headless). Like `--config`, the flag must be followed by a path
- `--output-dir dir` and `--save-config` apply to headless runs.
- `--script-tests` runs bundled Lua module tests.

## Planned and Completed Features (in rough priority order)
Some notes on terminology:

*Shuffle* - All items are pooled together and selected and removed from the pool. If the pool is empty, it will be reset. Each item will be used once before an item is used a second time </br>
*Randomize* - All items are pooled together and randomly chosen from. Items may be used many times or not at all

Major Feature List and Roadmap Key: </br>
⚪ Not implemented </br>
🔘 Present in latest major release </br>
🟢 Added in develop to be included in next major release </br>
🔵 Planned to be included in next major release </br>
🟡 Candidates to be included in next major release </br>
Features that end with a "?" are ones that may or may not be feasible to do that need to be looked into further
```
🟢 Create a basic friendly GUI to expand upon as features are added
🟢 Selecting Rom input and output names/paths </br>
🔵 Card Attacks/Powers/Effects
    🟢 Update/Replace card names in attacks/powers </br>
        🟢 Fully random/shuffle attacks
	    🟢 Within types
	    🟢 Change type energies to card type
	🟢 Fully random/shuffle powers
	    🟢 Include with moves
	    🟢 Within types
 	🟢 Randomize number of attacks for cards (based on original average moves per card)
🟢 Seed & Log
    🟢 Set seed
    🟢 Log seed
    🟢 Optionally log changes
🟡 Advance Move Tweaks
    🟡 Update/Replace energy type specific effects (e.g. ember, energy trans)
    🟡 Update/Replace specific effects (e.g. call for family))
    🟡 Update/Replace boyfriends with random, same type, 3rd stage evo
    🟡 Randomize trainer effects with powers?
⚪ HP, Retreat Cost
    ⚪ Fully random/shuffle
    ⚪ Evo chain progressing - swap stats to make higher evos more powerful/higher retreat cost
    ⚪ Evo chain consistent - some chains have generally higher HP, others have generally lower HP)
⚪ Weakness and Resistance
    ⚪ Fully random/shuffle
        ⚪ Make consistent across "types" (e.g. "Rock" or "Fighting" weakness and resistance)
        ⚪ Evo chain consistent - chains generally have the same weaknessess and resistances
        ⚪ Allow colorless weakness?
    ⚪ Multiple weaknesses/resistances
⚪ "Power" Based Moves (more balanced randomization)
    ⚪ Create power levels for cards, effects and damage
    ⚪ Assign moves based on card power levels
    ⚪ Randomize power levels of cards based on stage, apply appropriate rarities
    ⚪ Semi order (weight) through evo chains so later evos have higher powered moved
⚪ Randomize Card Types
    ⚪ Fully random
    ⚪ Consistent in evo lines (done after randomizing evo lines)
⚪ Randomize Evolutions
    ⚪  Fully random/shuffle
        ⚪ Include only multistage evolutions
        ⚪ Shuffle within stages
        ⚪ Force changes
    ⚪ Reassigning dex numbers to group them correctly in deck editor
    ⚪ Remove evolutions (all basic)
⚪ Decks
    ⚪ Random "type"/energy themed
    ⚪ Random multitype/energy themed
    ⚪ Random Keep same number of card categories (energies, trainers, monsters)
    ⚪ Evolution sanity
⚪ Advance & Customizable General Randomizations
    ⚪ Additional knobs for forcing damaging moves
    ⚪ User specifyable "types" - weakness/resistance pairs
    ⚪ User specified HP & retreat cost ranges
    ⚪ Per energy percentage based, user inputtable Card types distributions
    ⚪ Per stage percentage based, user input-able evolutions distributions
    ⚪ Per stage/evos left & per HP value percentage based, user input-able HP distributions
    ⚪ Per stage/evos left & per retreat cost percentage based, user input-able retreat cost distributions
    ⚪ Per stage/evos left percentage based, user input-able number of attacks/Powers distributions
    ⚪ Per "type" percentage based, user inputtable weakness/resistance distributions
    ⚪ Percentage based/user inputtable decks (knobs TBD)
⚪ Move Generation
    ⚪ Generate moves based on power levels
		⚪ User tweakable power scaling
    ⚪ Smart name generation
        ⚪ User inputtable names optionally type specific
    ⚪ Include trainer effects
⚪ Trainer Pics
⚪ Trades/Promo Cards
    ⚪ Fully Random
        ⚪ Within promos
        ⚪ Any card
⚪ Miscellaneous Tweaks
    🟢 Make all colorless
    🟢 Fix card name spelling errors (Ninetails vs Ninetales)
    ⚪ Booster pack adjustements
        ⚪ Change number of cards in booster packs (max 11)
        🟡 Change number of packs awarded after win (min ?, max ?)
        ⚪ Change card rarity distribution
    ⚪ Include promo cards in booster packs?
        ⚪ Exclude 4 legendary cards from packs
    ⚪ Unlimited trades?
```