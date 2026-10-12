
/* ==========================================
   TraceBack - UI Interactions
   Theme Toggle, History Search, Copy Hash
   ========================================== */

document.addEventListener("DOMContentLoaded", function () {

    // ----------------------------------
    // 1. Dark / Light Theme
    // ----------------------------------

    const themeToggle = document.getElementById("themeToggle");
    const root = document.documentElement;

    function applyTheme(theme) {
        const selectedTheme =
            theme === "light" ? "light" : "dark";

        root.setAttribute("data-theme", selectedTheme);

        if (themeToggle) {
            if (selectedTheme === "light") {
                themeToggle.textContent = "☾ Dark mode";
                themeToggle.setAttribute(
                    "aria-label",
                    "Switch to dark mode"
                );
            } else {
                themeToggle.textContent = "☀ Light mode";
                themeToggle.setAttribute(
                    "aria-label",
                    "Switch to light mode"
                );
            }
        }

        try {
            localStorage.setItem("traceback-theme", selectedTheme);
        } catch (error) {
            // Theme still works for the current page
            // even if browser storage is unavailable.
        }
    }

    let savedTheme = "dark";

    try {
        savedTheme =
            localStorage.getItem("traceback-theme") || "dark";
    } catch (error) {
        savedTheme = "dark";
    }

    applyTheme(savedTheme);

    if (themeToggle) {
        themeToggle.addEventListener("click", function () {
            const currentTheme =
                root.getAttribute("data-theme") || "dark";

            const nextTheme =
                currentTheme === "dark" ? "light" : "dark";

            applyTheme(nextTheme);
        });
    }


    // ----------------------------------
    // 2. Search Analysis History
    // ----------------------------------

    const searchInput = document.getElementById("searchInput");
    const historyTable = document.getElementById("historyTable");
    const noResults = document.getElementById("noResults");

    if (searchInput && historyTable) {

        const tableBody = historyTable.querySelector("tbody");

        if (tableBody) {

            searchInput.addEventListener("input", function () {

                const query =
                    searchInput.value.trim().toLowerCase();

                const rows = tableBody.querySelectorAll(
                    "tr:not(#noResults)"
                );

                let visibleCount = 0;

                rows.forEach(function (row) {

                    // Keep the initial empty-state message unchanged.
                    if (row.querySelector(".empty-state")) {
                        return;
                    }

                    const rowText =
                        row.textContent.toLowerCase();

                    const matches = rowText.includes(query);

                    row.hidden = !matches;

                    if (matches) {
                        visibleCount++;
                    }
                });

                if (noResults) {
                    noResults.hidden =
                        query.length === 0 || visibleCount > 0;
                }
            });
        }
    }


    // ----------------------------------
    // 3. Copy SHA-256 Hash
    // ----------------------------------

    const copyButtons = document.querySelectorAll(
        ".copy-btn[data-hash]"
    );

    copyButtons.forEach(function (button) {

        button.addEventListener("click", async function () {

            const hash = button.getAttribute("data-hash");

            if (!hash) {
                return;
            }

            const originalText = button.textContent;

            async function copyUsingFallback(text) {
                const textArea = document.createElement("textarea");

                textArea.value = text;
                textArea.setAttribute("readonly", "");
                textArea.style.position = "fixed";
                textArea.style.opacity = "0";

                document.body.appendChild(textArea);
                textArea.select();

                let successful = false;

                try {
                    successful = document.execCommand("copy");
                } catch (error) {
                    successful = false;
                }

                document.body.removeChild(textArea);

                if (!successful) {
                    throw new Error("Copy failed");
                }
            }

            try {

                if (navigator.clipboard &&
                    window.isSecureContext) {

                    await navigator.clipboard.writeText(hash);

                } else {

                    await copyUsingFallback(hash);
                }

                button.textContent = "COPIED";
                button.setAttribute("aria-label", "Hash copied");

                setTimeout(function () {
                    button.textContent = originalText;
                    button.setAttribute("aria-label", "Copy hash");
                }, 1600);

            } catch (error) {

                button.textContent = "FAILED";

                setTimeout(function () {
                    button.textContent = originalText;
                }, 1600);
            }
        });
    });

});
