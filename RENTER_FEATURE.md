# QuickRide - Renter Feature

This branch contains the Renter feature implementation for the QuickRide Vehicle Renter Management application.

## Overview
The Renter feature allows vehicle renters to manage their vehicle fleet through the application.

## Features
- **Renter Dashboard**: View all vehicle categories (Cars, Vans, Motorbikes, Buses, Tuktuk)
- **Manage Vehicles**: Add, update, and remove vehicles for each category
- **Vehicle Management**:
  - Add vehicle with details (number/plate, type, price per day)
  - Upload one or more vehicle images
  - Update existing vehicle information
  - Remove vehicles from the fleet

## Files Included

### Java/Kotlin Files
- `app/src/main/java/com/example/myapplication/renter/RenterDashboardActivity.java`
- `app/src/main/java/com/example/myapplication/renter/ManageVehiclesActivity.java`
- `app/src/main/java/com/example/myapplication/renter/AddEditVehicleActivity.java`

### Layout Files
- `app/src/main/res/layout/activity_renter_dashboard.xml`
- `app/src/main/res/layout/activity_manage_vehicles.xml`
- `app/src/main/res/layout/activity_add_edit_vehicle.xml`

### AndroidManifest Entries
The following activities are registered in AndroidManifest.xml:
```xml
<activity android:name=".renter.RenterDashboardActivity" android:exported="true"/>
<activity android:name=".renter.ManageVehiclesActivity" android:exported="true"/>
<activity android:name=".renter.AddEditVehicleActivity" android:exported="true"/>
```

### String Resources Used
The following string resources are used by the renter feature (defined in `strings.xml`):
- `screen_renter_dashboard`
- `welcome_renter`
- `vehicle_cars`, `vehicle_vans`, `vehicle_motorbikes`, `vehicle_buses`, `vehicle_tuktuk`
- `vehicle_description_*` strings
- `btn_manage`, `btn_add_vehicle`, `btn_update_vehicle`, `btn_remove_vehicle`, `btn_add_image`
- `hint_vehicle_number`, `hint_vehicle_type`, `hint_price_per_day`
- `vehicle_images`, `vehicle_images_note`, `tap_to_add_image`, `no_vehicles_message`

## UI Design
The UI follows Material Design 3 principles with:
- Material Toolbar
- Card-based layouts
- Floating Action Buttons
- Material Text Input Fields
- Image upload functionality

## Note
The renter vehicle image flow is now implemented with direct Cloudinary uploads, and the returned image URL is saved in Firestore.

## Integration
To integrate the renter feature in the main app:
1. Include the renter package files
2. Add the layout files
3. Register activities in AndroidManifest.xml
4. Ensure string resources are available in values/strings.xml
5. Add navigation from RoleSelectionActivity to RenterDashboardActivity

## Navigation Flow
```
RoleSelectionActivity
    └── [Renter Button] → RenterDashboardActivity
                            └── [Manage Button] → ManageVehiclesActivity
                                                    └── [Add/Edit] → AddEditVehicleActivity
```
