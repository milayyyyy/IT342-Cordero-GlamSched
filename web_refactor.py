import os, re, shutil

SRC = r"c:\Users\Admin\Desktop\glamshed-booking-platform\IT342-Cordero-GlamSched\web\src"

# Map: original relative path (from SRC) -> new relative path (from SRC)
FILE_MAP = {
    # shared
    r"utils\api.js": r"shared\api.js",
    # auth feature
    r"pages\Login.js": r"features\auth\Login.js",
    r"pages\Register.js": r"features\auth\Register.js",
    # dashboard feature
    r"pages\Dashboard.js": r"features\dashboard\Dashboard.js",
    r"pages\Dashboard.css": r"features\dashboard\Dashboard.css",
    # booking feature
    r"pages\BookAppointment.js": r"features\booking\BookAppointment.js",
    r"pages\BookingFlow.js": r"features\booking\BookingFlow.js",
    r"pages\ArtistAppointments.js": r"features\booking\ArtistAppointments.js",
    r"pages\MyAppointments.js": r"features\booking\MyAppointments.js",
    r"styles\BookAppointment.css": r"features\booking\BookAppointment.css",
    r"styles\BookingFlow.css": r"features\booking\BookingFlow.css",
    r"styles\ArtistAppointments.css": r"features\booking\ArtistAppointments.css",
    # services feature
    r"pages\BrowseServices.js": r"features\services\BrowseServices.js",
    r"components\AddServiceModal.js": r"features\services\AddServiceModal.js",
    r"styles\AddServiceModal.css": r"features\services\AddServiceModal.css",
    r"styles\Services.css": r"features\services\Services.css",
    # payment feature
    r"pages\PaymentHistory.js": r"features\payment\PaymentHistory.js",
    r"pages\PaymentPage.js": r"features\payment\PaymentPage.js",
    r"styles\PaymentPage.css": r"features\payment\PaymentPage.css",
    # review feature
    r"pages\LeaveReview.js": r"features\review\LeaveReview.js",
    r"styles\LeaveReview.css": r"features\review\LeaveReview.css",
    # user feature
    r"pages\UserProfile.js": r"features\user\UserProfile.js",
    r"styles\UserProfile.css": r"features\user\UserProfile.css",
}

def compute_relative_import(from_new_path, to_new_path):
    """Compute relative import path from from_new_path to to_new_path (both relative to SRC, no extension)."""
    from_dir = os.path.dirname(from_new_path)
    rel = os.path.relpath(to_new_path, from_dir).replace("\\", "/")
    if not rel.startswith("."):
        rel = "./" + rel
    # Remove extension for JS imports
    for ext in [".js", ".css"]:
        if rel.endswith(ext):
            rel = rel[:-len(ext)]
            break
    return rel

# Build reverse lookup: filename stem -> new path (without SRC prefix)
# e.g. "api" -> r"shared\api.js"
old_to_new = {}
for old_rel, new_rel in FILE_MAP.items():
    old_to_new[old_rel] = new_rel

# Build stem-based lookup for import resolution
# Maps (old_dir_pattern, stem) -> new_relative_path
stem_to_new = {}
for old_rel, new_rel in FILE_MAP.items():
    stem = os.path.splitext(os.path.basename(old_rel))[0]
    old_dir = os.path.dirname(old_rel)
    stem_to_new[(old_dir, stem)] = new_rel
    stem_to_new[("*", stem)] = new_rel  # wildcard fallback

def fix_imports_in_file(file_content, current_new_rel):
    """Fix import paths in a JS file that's been moved to current_new_rel."""
    def replace_import(m):
        imp_stmt = m.group(0)
        imp_path = m.group(1)
        
        if not imp_path.startswith("."):
            return imp_stmt  # external package, don't touch
        
        # Resolve the old path the import was pointing to
        # current_new_rel is the new location of the file
        # We need to figure out what the OLD location was
        old_rel = None
        for o, n in FILE_MAP.items():
            if n == current_new_rel:
                old_rel = o
                break
        
        if old_rel is None:
            # This file wasn't moved (e.g. App.js), handle from its current location
            old_rel = current_new_rel
        
        old_dir = os.path.dirname(old_rel)
        
        # Normalize the import path (remove ./ prefix for resolution)
        clean_path = imp_path
        if clean_path.startswith("./"):
            clean_path = clean_path[2:]
        elif clean_path.startswith("../"):
            pass  # keep as is for resolution
        
        # Resolve the imported file relative to old location
        # e.g. from pages/Dashboard.js, import './BookAppointment' -> pages/BookAppointment.js
        old_import_dir = os.path.dirname(os.path.join(old_dir, imp_path))
        old_import_stem = os.path.basename(imp_path)
        # Remove extension if present
        if "." in old_import_stem:
            old_import_stem_noext, ext = os.path.splitext(old_import_stem)
        else:
            old_import_stem_noext = old_import_stem
            ext = ""
        
        # Normalize: resolve the full old path
        resolved_old = os.path.normpath(os.path.join(old_dir, imp_path))
        resolved_old_noext = os.path.splitext(resolved_old)[0]
        
        # Find what this maps to in FILE_MAP
        new_target = None
        for old_key, new_val in FILE_MAP.items():
            old_key_noext = os.path.splitext(old_key)[0]
            if resolved_old_noext.replace("/", "\\") == old_key_noext.replace("/", "\\"):
                new_target = new_val
                break
            if resolved_old.replace("/", "\\") == old_key.replace("/", "\\"):
                new_target = new_val
                break
        
        if new_target is None:
            return imp_stmt  # couldn't map, leave unchanged
        
        # Compute new relative import from current_new_rel to new_target
        new_imp = compute_relative_import(current_new_rel, new_target)
        
        return imp_stmt.replace(f'"{imp_path}"', f'"{new_imp}"').replace(f"'{imp_path}'", f"'{new_imp}'")
    
    pattern = re.compile(r"""(?:import\s+[^'"]*from\s+|import\s+)['"](\.\.?/[^'"]+)['"]""")
    return pattern.sub(replace_import, file_content)

# First, read all files
file_contents = {}
for old_rel, new_rel in FILE_MAP.items():
    old_abs = os.path.join(SRC, old_rel)
    if os.path.exists(old_abs):
        with open(old_abs, "r", encoding="utf-8") as f:
            file_contents[old_rel] = f.read()
    else:
        print(f"WARNING: {old_abs} not found")

# Also read App.js
app_js_path = os.path.join(SRC, "App.js")
with open(app_js_path, "r", encoding="utf-8") as f:
    app_js_content = f.read()

# Create new files with updated imports
for old_rel, new_rel in FILE_MAP.items():
    if old_rel not in file_contents:
        continue
    
    content = file_contents[old_rel]
    
    # Only process JS files for import updates
    if new_rel.endswith(".js"):
        content = fix_imports_in_file(content, new_rel)
    
    # Write to new location
    new_abs = os.path.join(SRC, new_rel)
    os.makedirs(os.path.dirname(new_abs), exist_ok=True)
    with open(new_abs, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Created: {new_rel}")

# Update App.js
app_js_fixed = fix_imports_in_file(app_js_content, "App.js")
with open(app_js_path, "w", encoding="utf-8") as f:
    f.write(app_js_fixed)
print("Updated: App.js")

print("\nDone! Please review the updated files.")
