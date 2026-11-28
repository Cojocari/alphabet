# App Store Connect: what to fill in

Everything the App Store asks for Hupi the Owl, in the order App Store Connect shows it. The one-time setup
(App ID, app record, provisioning profile, Codemagic) is in `docs/ios.md`, "App Store". The long descriptions
are the Google Play ones in `docs/store-listing.md`; this file only adds what Apple asks for and Google
doesn't.

---

## 0. Business (once per team, before the first paid product)
*App Store Connect → Business*
- **Paid Apps Agreement** must be **Active**; without it the in-app purchase never appears in the app.
  Sign it, then add the bank account (the company's BT account, as on Google Play) and the tax forms:
  **W-8BEN-E** with the same answers as Google's (corporation, Romania, Foreign TIN 49413417 without RO,
  treaty Romania, no limitation on benefits article, no US activities).
- **EU Digital Services Act**: declare **trader** status with the company's address, phone and
  `support@cojocarisoft.com`; Apple shows them on the EU product page.

## 1. App Information
| Field | English (U.S.), primary | Romanian |
|---|---|---|
| Name (≤30) | Hupi the Owl: ABC & 123 Kids | Bufnița Isteață: ABC și 123 |
| Subtitle (≤30) | Learn letters, numbers & math | Litere, cifre și socoteli |

Add Romanian with the language menu at the top right (**+ → Romanian**). Each language has its own name,
subtitle, description, keywords, promotional text and screenshots.

- **Category:** primary **Education**; tick **Kids**, age range **5 and under**. Kids apps follow
  guideline 1.3: no ads, no third-party analytics, a parental gate before purchases and links out (all done).
- **Content Rights:** *Yes, it contains third-party content* (the animal recordings,
  `docs/sound-credits.md`) → *and I have the rights to use it*.
- **Age Rating:** answer **None / No** to everything (violence, fear, gambling, contests, web access,
  user-generated content, messaging, advertising, age assurance). Parental controls: **Yes** (daily
  play-time limit in the grown-ups' corner). Expected rating **4+**.
- **License Agreement:** keep Apple's standard EULA; our terms are linked from the description and the
  website (`https://cojocarisoft.com/hupi/terms.html`).

## 2. Pricing and Availability
- Price **Free** (the unlock is the in-app purchase).
- Availability: all countries and regions **except China mainland**. China needs an ICP filing number for
  every app, which we don't have. Untick it, or the review stops.

## 3. App Privacy
- Privacy Policy URL: `https://cojocarisoft.com/hupi/privacy.html`
- *Do you or your third-party partners collect data from this app?* → **No, we do not collect data**.
  Publish. (The privacy manifest `iosApp/iosApp/PrivacyInfo.xcprivacy` says the same.)

## 4. The version page (iOS App → 4.8)

**Screenshots** (`branding/screenshots/appstore/<lang>/`, made by `sh branding/frame-appstore.sh`):
- **iPhone 6.9" Display**: `iphone-1…8.png` (1320 × 2868). Apple scales them down for smaller iPhones.
- **iPad 13" Display**: `ipad-1…8.png` (2064 × 2752).

**Promotional text** (≤170, can change any time without a review):
- EN: `New: Hupi's Adventure, a 7-chapter story where your child teaches Hupi letters, numbers and colours. No ads, no subscriptions: one payment unlocks everything.`
- RO: `Nou: Aventura lui Hupi, o poveste în 7 capitole în care copilul îl învață pe Hupi litere, cifre și culori. Fără reclame, fără abonamente, o singură plată.`

**Description:** the full descriptions from `docs/store-listing.md` (English and Romanian), unchanged.

**Keywords** (≤100, commas, no spaces; words already in the name or subtitle count anyway, so they're left out):
- EN: `alphabet,phonics,reading,tracing,counting,preschool,toddler,kindergarten,colors,shapes,animals`
- RO: `alfabet,citire,scriere,numere,matematica,copii,gradinita,prescolari,jocuri,educativ,culori,animale`
  (without diacritics, as parents type them)

| Field | Value |
|---|---|
| Support URL | `https://cojocarisoft.com/hupi/` |
| Marketing URL | `https://cojocarisoft.com/hupi/` |
| Version | 4.8 |
| Copyright | 2026 COJOCARI SOFT SRL |
| Build | the Codemagic upload (4.8, build 14.x), once Apple has processed it |
| In-App Purchases | **+ → `unlock_all`**, so it is reviewed together with the app |
| Version release | **Manually release this version** (you choose the day, like Managed publishing on Play) |

**App Review Information:**
- Sign-in required: **no** (the app has no accounts).
- Contact: Adrian Cojocari, your phone, `support@cojocarisoft.com`.
- Notes (paste, then replace `<CODE>` with the reviewer code from Play Console → App access; never put the
  code in the repository):
```
Hupi the Owl is a learning game for children aged 2-6. There are no accounts, ads, analytics or data collection.

Parental gate: the grown-ups' corner (gear button on the home screen), the purchase screen and every link out of the app ask an addition question "For grown-ups: what is a + b?" with four answers to choose from.

In-app purchase: one non-consumable product, unlock_all ("Unlock everything"). It can be bought from the end of any demo or from the grown-ups' corner, and restored with the Restore button on the purchase screen.

To see all content without buying: grown-ups' corner -> "Have a code?" -> <CODE>
```

## 5. The in-app purchase
*Monetization → In-App Purchases → +*
| Field | Value |
|---|---|
| Type | **Non-Consumable** |
| Reference name | Unlock everything |
| Product ID | **`unlock_all`** (exactly this; the app asks for it) |
| Price | base country Romania, **24.99 lei** (Apple fills in the other countries) |
| Family Sharing | **on** |
| Display name (≤30) | EN `Unlock everything` · RO `Deblochează tot` |
| Description (≤45) | EN `All games, the whole story and Hard levels` · RO `Toate jocurile, povestea și nivelul Greu` |
| Review screenshot | `branding/screenshots/appstore/en/iap-review.png` (the purchase screen) |
| Review notes | `Unlocks every game, the whole story mode and the Hard level, forever.` |

Its status must reach **Ready to Submit**. Then add it to the version page (section 4) and submit both.

## 6. Submit
**Add for Review → Submit**. The first review usually takes **1–3 days**. With "Manually release" chosen, the
app waits for you to press **Release this version** once it is approved.
