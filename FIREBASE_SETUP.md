# MediBuzz Firebase Setup

## 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add project** → name it `MediBuzz`
3. Disable Google Analytics (optional for MVP)
4. Click **Create project**

## 2. Register Android App

1. In Firebase Console, click **Add app** → Android
2. Package name: `com.medibuzz`
3. Download `google-services.json`
4. Replace `app/google-services.json` in this project with the downloaded file

## 3. Enable Authentication

1. Firebase Console → **Build** → **Authentication**
2. Click **Get started**
3. Enable **Email/Password** sign-in method

## 4. Enable Firestore

1. Firebase Console → **Build** → **Firestore Database**
2. Click **Create database**
3. Start in **production mode**
4. Choose a region close to your users

## 5. Deploy Security Rules

1. Firebase Console → **Firestore** → **Rules**
2. Copy contents from `firestore.rules` in this project
3. Click **Publish**

Or use Firebase CLI:

```bash
firebase login
firebase init firestore
firebase deploy --only firestore:rules
```

## 6. Firestore Indexes

The partner dashboard queries `shared_status` by `carePartnerId` and `scheduledTime`. If Firebase prompts for an index, create a composite index:

- Collection: `shared_status`
- Fields: `carePartnerId` (Ascending), `scheduledTime` (Ascending)

## 7. Build and Run

1. Open `MediBuzz` in Android Studio
2. Sync Gradle
3. Run on device or emulator with internet access

## Collections Structure

### users
| Field | Type | Description |
|-------|------|-------------|
| uid | string | Firebase Auth UID |
| email | string | User email |
| role | string | `MEDICINE_USER` or `CARE_PARTNER` |
| displayName | string | Display name |
| partnerCode | string | 8-char code (medicine users only) |
| createdAt | number | Timestamp |

### partner_links (document ID = medicineUserId)
| Field | Type | Description |
|-------|------|-------------|
| medicineUserId | string | Medicine user's UID |
| carePartnerId | string | Care partner's UID |
| sharingEnabled | boolean | Sharing toggle |
| createdAt | number | Timestamp |

### shared_status
| Field | Type | Description |
|-------|------|-------------|
| medicineUserId | string | Medicine user's UID |
| carePartnerId | string | Care partner's UID |
| medicineId | number | Local Room medicine ID |
| medicineName | string | Medicine name |
| scheduledTime | number | Scheduled dose time |
| status | string | PENDING, TAKEN, SNOOZED, SKIPPED, MISSED |
| confirmedTime | number | When user confirmed (optional) |
| updatedAt | number | Last sync time |

## Troubleshooting

- **Login fails**: Verify Email/Password auth is enabled
- **Partner code not found**: Ensure medicine user registered as MEDICINE_USER
- **Partner sees no data**: Medicine user must enable sharing in Sharing Settings
- **Sync fails offline**: Local Room data still works; sync resumes when online
- **Build fails on google-services**: Replace placeholder `google-services.json` with real file from Firebase
