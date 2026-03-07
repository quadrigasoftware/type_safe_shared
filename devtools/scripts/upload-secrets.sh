#!/bin/bash

# Usage: upload-secrets.sh <env-file-path> [--project <project-id>] [--dry-run] [key1] [key2] ...
# Example: upload-secrets.sh .env --project my-dev-project --dry-run SESSION_SECRET

ENV_FILE=$1
shift

PROJECT_ID=""
DRY_RUN=false
SPECIFIC_KEYS=""
UPLOAD_ALL=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --project)
            PROJECT_ID="$2"
            shift 2
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --all)
            UPLOAD_ALL=true
            shift
            ;;
        *)
            SPECIFIC_KEYS="$SPECIFIC_KEYS $1 "
            shift
            ;;
    esac
done

if [ -z "$ENV_FILE" ]; then
    echo "Usage: $0 <env-file-path> [--project <id>] [--dry-run] <key1> [key2] ... OR --all"
    exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
    echo "Error: $ENV_FILE not found."
    exit 1
fi

# ---------------------------------------------------
# FATAL AUTH CHECK
# ---------------------------------------------------
if ! gcloud auth print-access-token > /dev/null 2>&1; then
    echo "---------------------------------------------------"
    echo "❌ FATAL ERROR: Not logged in to gcloud CLI or session expired."
    echo "Please run: gcloud auth login"
    echo "---------------------------------------------------"
    exit 1
fi

CURRENT_ACCOUNT=$(gcloud config get-value account 2>/dev/null)

# Detect current project if not provided
if [ -z "$PROJECT_ID" ] || [ "$PROJECT_ID" == "(unset)" ]; then
    PROJECT_ID=$(gcloud config get-value project 2>/dev/null)
fi

if [ -z "$PROJECT_ID" ] || [ "$PROJECT_ID" == "(unset)" ]; then
    echo "---------------------------------------------------"
    echo "❌ FATAL ERROR: No GCP project selected."
    echo "Please run: gcloud config set project <project-id>"
    echo "Or use the --project flag."
    echo "---------------------------------------------------"
    exit 1
fi

echo "---------------------------------------------------"
echo "  GCP SECRET UPLOAD"
echo "  Target Project: $PROJECT_ID"
echo "  Account:        $CURRENT_ACCOUNT"
echo "  Source File:    $ENV_FILE"
if [ "$DRY_RUN" = true ]; then
    echo "  MODE:           🚀 DRY RUN (No changes will be made)"
else
    echo "  MODE:           🔥 REAL RUN"
fi
echo "---------------------------------------------------"

if [ "$UPLOAD_ALL" = true ]; then
    echo "⚠️  WARNING: You are about to upload ALL variables."
else
    if [ -z "$SPECIFIC_KEYS" ]; then
        echo "Error: You must provide at least one secret key or use --all."
        exit 1
    fi
    echo "Uploading specific keys: $SPECIFIC_KEYS"
fi

if [ "$DRY_RUN" = false ]; then
    echo -n "Confirm these details? (yes/N): "
    read confirm
    if [[ $confirm != "yes" ]]; then
        echo "Aborted."
        exit 0
    fi
fi

# Function to upload a secret
upload_secret() {
    local key=$1
    local value=$2
    local project=$3
    
    # ---------------------------------------------------
    # EMPTY PAYLOAD CHECK
    # ---------------------------------------------------
    if [ -z "$value" ]; then
        echo "Skipping: $key (Empty payload)"
        return
    fi
    
    if [ "$DRY_RUN" = true ]; then
        echo "[DRY RUN] Would process: $key"
        return
    fi
    
    echo "Processing: $key..."
    
    # Check if exists
    if gcloud secrets describe "$key" --project="$project" > /dev/null 2>&1; then
        echo -n "$value" | gcloud secrets versions add "$key" --project="$project" --data-file=- > /dev/null
    else
        gcloud secrets create "$key" --project="$project" --replication-policy="automatic" > /dev/null
        echo -n "$value" | gcloud secrets versions add "$key" --project="$project" --data-file=- > /dev/null
    fi
}

FOUND_KEYS_STR=""
while IFS='=' read -r key value || [ -n "$key" ]; do
    [[ "$key" =~ ^#.*$ ]] && continue
    [[ -z "$key" ]] && continue
    # Clean value (handle potential trailing whitespace/quotes)
    value=$(echo "$value" | sed -e 's/^"//' -e 's/"$//' -e "s/^'//" -e "s/'$//")
    value=$(echo "$value" | xargs) # trim whitespace

    if [ "$UPLOAD_ALL" = true ]; then
        upload_secret "$key" "$value" "$PROJECT_ID"
    else
        if [[ "$SPECIFIC_KEYS" =~ " $key " ]]; then
            upload_secret "$key" "$value" "$PROJECT_ID"
            FOUND_KEYS_STR="$FOUND_KEYS_STR $key "
        fi
    fi
done < "$ENV_FILE"

# Missing keys check
if [ "$UPLOAD_ALL" = false ] && [ "$DRY_RUN" = false ]; then
    for k in $SPECIFIC_KEYS; do
        if [[ ! "$FOUND_KEYS_STR" =~ " $k " ]]; then
            echo "Error: Key '$k' not found in $ENV_FILE."
        fi
    done
fi

echo "Done."
