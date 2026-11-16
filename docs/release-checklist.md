# Release checklist: Google Play

Step by step, from zero to the app on Google Play. Tick each box as you go.

**Ready now (16 November 2026):** release bundle, store texts in Romanian and English (`docs/store-listing.md`,
including release notes), icon, feature graphics, 8 phone and 8 tablet screenshots per language
(`branding/screenshots/`), privacy policy text. **Only you can do:** the developer account (step 4),
publishing the privacy policy (step 3), and everything inside Play Console (steps 5–11).

---

## 0. Already done in the code
- [x] Targets Android 16 (API 36), as Google Play requires for new apps
- [x] Google Play Billing 8, one-time product `unlock_all`
- [x] Parental gate before purchases, settings and links out (Families Policy)
- [x] No ads, no analytics, no data collection; the child's name is only spoken offline
- [x] Release build shrunk and signed with the upload key
- [x] Developer "unlock without paying" switch exists only in debug builds
- [x] Contact email in the grown-ups' corner
- [x] Story mode and the learning diary save only on the device (covered in the privacy policy)
- [x] Store screenshots and texts updated for the story mode

## 1. Back up the signing key, today
The upload key signs every version you ever send to Google Play.
- [ ] Copy **`C:\Users\Adrian\.android-keys\alphabet-upload.jks`** and the project's
      **`keystore.properties`** (it holds the password) to a safe place: a USB stick and a
      password manager or cloud drive. Never put them on GitHub. Git already ignores them.

## 2. Build the file to upload
```powershell
.\gradlew bundleRelease
```
The file is **`app/build/outputs/bundle/release/app-release.aab`**.
Before every new upload, raise `versionCode` (and `versionName`) in `app/build.gradle.kts`.
Google rejects a bundle whose `versionCode` it has already seen.

## 3. Publish the privacy policy online
Google needs a public link (URL). The text is in `docs/privacy-policy.md`. Pick one:
- **GitHub Pages** (free; the repository is already public): on github.com open the repository →
  *Settings → Pages → Build and deployment → Source: Deploy from a branch → Branch: `develop`, folder `/docs` → Save*.
  After a minute the link is **`https://cojocari.github.io/alphabet/privacy-policy`**. Open it once to check.
  (Use `develop` because `main` only has the first commit; switch to `main` after merging.)
- **Google Sites** (free, works with a private repository): create a page, paste the text,
  publish, and use that link.

## 4. Google Play developer account
- [ ] Sign up at https://play.google.com/console ($25, one time). Choose a **personal** account,
      or **organisation** if you have a company (PFA/SRL). Organisations skip step 10's testing rule.
- [ ] Verify your identity (ID document) and phone.
- [ ] *Settings → Payments profile*: name, address, **bank account (IBAN)**. Payouts arrive monthly.
- [ ] Ask an accountant how to declare the income in your country.

## 5. Create the app
*Play Console → Create app*
- [ ] App name: **Clever Owl: ABC & 123 for Kids**, default language **English (United States)**; then add the Romanian listing as a translation (see `store-listing.md`)
- [ ] App, **Free** (the unlock is an in-app purchase; the app download itself must be free)
- [ ] Accept the declarations

## 6. App content (*Policy → App content*)
- [ ] **Privacy policy**: the link from step 3
- [ ] **Ads**: *No, my app does not contain ads*
- [ ] **App access**: *All functionality is available without special access*
- [ ] **Content rating** (questionnaire): category *Reference, news or educational*;
      answer **No** to violence, fear, gambling, user interaction, sharing location, etc.
      Expected result: PEGI 3 / Everyone.
- [ ] **Target audience and content**: age groups **5 and under** and **6–8**.
      This enrols the app in the **Families Policy**: no ads, gate before purchases (already done).
- [ ] **Data safety**:
  - *Does your app collect or share any of the required user data types?* → **No**
  - *Is all data encrypted in transit?* → not applicable (nothing is sent)
  - *Account creation / deletion* → **No accounts**
- [ ] Government app → No · Financial features → None · Health → No · News → No

## 7. Store listing (*Grow users → Store presence → Main store listing*)
- [ ] Texts from `docs/store-listing.md`, English as the main listing, Romanian as a translation
- [ ] App icon and feature graphic: ready in `branding/` (`icon-512.png`, `feature-graphic-ro.png`, `feature-graphic-en.png`)
- [ ] Screenshots, ready in `branding/screenshots/`: `ro/phone-*.jpg` and `ro/tablet-*.jpg` on the Romanian
      listing, `en/…` on the English one. Upload the tablet set for both 7-inch and 10-inch.
- [ ] Category **Education**, contact email **cojocari.adrianp@gmail.com**

## 8. The in-app product
*Monetize with Play → Products → In-app products → Create product*
- [ ] Product ID: **`unlock_all`** (exactly this; the app looks for it)
- [ ] Name and description from `docs/store-listing.md`
- [ ] Price: e.g. **24,99 lei** for Romania (equivalent in other countries, set automatically or by hand)
- [ ] **Activate** the product
- [ ] Optional: turn on **Family sharing** for it (*Monetize → Family Library*)
- Note: the product can only be created after a first bundle has been uploaded (step 9).

## 9. Internal testing, with real purchase flow and no money
*Testing → Internal testing → Create new release*
- [ ] Upload `app-release.aab`; accept **Play App Signing** (Google keeps the final key)
- [ ] Add testers: your Gmail address(es)
- [ ] *Settings → License testing*: add the same addresses. Their purchases use a test card and are free.
- [ ] Open the opt-in link on the tablet, install from Play (uninstall the cable-installed version first)
- [ ] Test the flow: demo ends → unlock → gate → buy with **Test card, always approves** → everything unlocks
- [ ] Test **Restore**: uninstall, reinstall, open → everything is still unlocked

## 10. Closed testing (required for new personal accounts)
New personal accounts must run a **closed test with at least 12 testers for 14 days in a row**
before they can publish to everyone.
- [ ] *Testing → Closed testing*: create a track, add 12+ Gmail addresses (family, friends, parents
      from kindergarten). Each must accept the invite link and keep the app installed for 14 days.
- [ ] Ask them to really play and send feedback with the 🐞 / 💡 buttons.
- [ ] After 14 days: *Dashboard → Apply for production* and answer the short questionnaire.

## 11. Production
- [ ] *Production → Create new release*: same or newer bundle
- [ ] Countries: Romania and Moldova first (plus any others you want)
- [ ] Optional: a **launch discount** on `unlock_all` for the first 2 weeks
- [ ] Submit for review (usually 1–7 days for children's apps)

## 12. After launch
- [ ] *Quality → Android vitals*: watch for crashes and ANRs
- [ ] Answer every review in Play Console; it builds trust and ranking
- [ ] Optional: apply for the **Teacher Approved** badge (*Store presence → Teacher Approved*);
      it gives a big visibility boost in the Kids tab
- [ ] For each update: raise `versionCode`, build, upload, write release notes in RO and EN
