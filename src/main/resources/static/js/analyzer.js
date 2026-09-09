/**
 * TitleVerify AI - Application Analyzer Client Logic
 */
(function () {
    const MAX_TITLES = 5;
    const MIN_TITLES = 1;

    function initAnalyzer() {
        const titlesListContainer = document.getElementById('titlesList');
        const addTitleBtn = document.getElementById('addTitleBtn');
        const titleLimitHint = document.getElementById('titleLimitHint');
        const analyzerForm = document.getElementById('analyzerForm');
        const submitBtn = document.getElementById('submitBtn');

        // Modal elements
        const confirmationModal = document.getElementById('confirmationModal');
        const modalCloseIconBtn = document.getElementById('modalCloseIconBtn');
        const modalCloseBtn = document.getElementById('modalCloseBtn');
        const modalContinueBtn = document.getElementById('modalContinueBtn');

        if (!analyzerForm) return;

        /**
         * Modal Dialog Control
         */
        let savedApplicationId = null;

        function openModal(data) {
            if (!confirmationModal) return;
            const appId = data ? (data.id || data.applicationId) : null;
            if (appId) {
                savedApplicationId = appId;
            }
            if (modalContinueBtn) {
                modalContinueBtn.textContent = "View Detailed Analysis \u2192";
            }
            confirmationModal.classList.add('show');
            confirmationModal.setAttribute('aria-hidden', 'false');
            document.body.style.overflow = 'hidden';
            if (modalContinueBtn) modalContinueBtn.focus();
        }

        function closeModal() {
            if (!confirmationModal) return;
            confirmationModal.classList.remove('show');
            confirmationModal.setAttribute('aria-hidden', 'true');
            document.body.style.overflow = '';
            if (submitBtn) submitBtn.focus();
        }

        // Modal Event Listeners
        if (modalCloseIconBtn) modalCloseIconBtn.addEventListener('click', closeModal);
        if (modalCloseBtn) modalCloseBtn.addEventListener('click', closeModal);
        if (modalContinueBtn) {
            modalContinueBtn.addEventListener('click', () => {
                if (savedApplicationId) {
                    window.location.href = '/analysis/' + savedApplicationId;
                } else {
                    alert('Unable to navigate: Application ID was not returned by the server.');
                    closeModal();
                }
            });
        }

        // Close on clicking backdrop overlay outside modal card
        if (confirmationModal) {
            confirmationModal.addEventListener('click', (e) => {
                if (e.target === confirmationModal) {
                    closeModal();
                }
            });
        }

        // Close on Escape key press
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && confirmationModal && confirmationModal.classList.contains('show')) {
                closeModal();
            }
        });

        /**
         * Re-indexes remaining title rows and manages remove button visibility
         */
        function updateTitleRows() {
            if (!titlesListContainer) return;
            const titleRows = titlesListContainer.querySelectorAll('.title-row');
            const count = titleRows.length;

            titleRows.forEach((row, index) => {
                const indexBadge = row.querySelector('.title-index');
                const input = row.querySelector('.title-input');
                const removeBtn = row.querySelector('.btn-remove-title');
                const errorMsg = row.querySelector('.error-message');

                const itemNumber = index + 1;
                if (indexBadge) indexBadge.textContent = itemNumber;
                if (input) {
                    input.id = `title_${itemNumber}`;
                    input.placeholder = `e.g. ${itemNumber === 1 ? 'Deccan Herald' : 'Times of India'}`;
                    input.setAttribute('aria-label', `Proposed Title Option ${itemNumber}`);
                }
                if (errorMsg) {
                    errorMsg.id = `titleError_${itemNumber}`;
                }
                if (removeBtn) {
                    removeBtn.setAttribute('aria-label', `Remove Title ${itemNumber}`);
                    // Hide remove button if only 1 title row remains
                    removeBtn.style.display = count > MIN_TITLES ? 'flex' : 'none';
                }
            });

            // Toggle Add button state
            if (addTitleBtn) {
                if (count >= MAX_TITLES) {
                    addTitleBtn.disabled = true;
                    if (titleLimitHint) titleLimitHint.textContent = `Maximum limit of ${MAX_TITLES} titles reached`;
                } else {
                    addTitleBtn.disabled = false;
                    if (titleLimitHint) titleLimitHint.textContent = `${count} of ${MAX_TITLES} title slots used`;
                }
            }
        }

        /**
         * Adds a new title row
         */
        function addTitleRow() {
            if (!titlesListContainer) return;
            const currentCount = titlesListContainer.querySelectorAll('.title-row').length;
            if (currentCount >= MAX_TITLES) return;

            const nextIndex = currentCount + 1;
            const newRow = document.createElement('div');
            newRow.className = 'title-row';
            newRow.innerHTML = `
                <div class="title-index" aria-hidden="true">${nextIndex}</div>
                <div class="title-input-wrapper">
                    <label for="title_${nextIndex}" class="sr-only">Proposed Title Option ${nextIndex}</label>
                    <input type="text" class="form-control title-input" id="title_${nextIndex}" placeholder="Enter proposed title option" autocomplete="off" aria-describedby="titleError_${nextIndex}" />
                    <span class="error-message" id="titleError_${nextIndex}" role="alert">Please enter a proposed title</span>
                </div>
                <button type="button" class="btn-remove-title" title="Remove Title" aria-label="Remove Title ${nextIndex}">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="18" y1="6" x2="6" y2="18"></line>
                        <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                </button>
            `;

            // Event listener for remove button
            const removeBtn = newRow.querySelector('.btn-remove-title');
            if (removeBtn) {
                removeBtn.addEventListener('click', () => {
                    newRow.remove();
                    updateTitleRows();
                });
            }

            // Clear error on input/change
            const input = newRow.querySelector('.title-input');
            if (input) {
                const clearRowError = () => {
                    input.classList.remove('error');
                    const err = newRow.querySelector('.error-message');
                    if (err) err.classList.remove('show');
                };
                input.addEventListener('input', clearRowError);
                input.addEventListener('change', clearRowError);
            }

            titlesListContainer.appendChild(newRow);
            updateTitleRows();
            if (input) input.focus();
        }

        // Attach click handler for Add Title button
        if (addTitleBtn) {
            addTitleBtn.addEventListener('click', addTitleRow);
        }

        // Attach clear error events for all controls inside form
        const setupControlErrorClearing = () => {
            const formControls = analyzerForm.querySelectorAll('.form-control');
            formControls.forEach(control => {
                const clearError = () => {
                    control.classList.remove('error');
                    const errorElem = document.getElementById(`${control.id}Error`) ||
                                      document.getElementById(`titleError_${control.id.replace('title_', '')}`) ||
                                      control.closest('.form-group, .title-input-wrapper')?.querySelector('.error-message');
                    if (errorElem) errorElem.classList.remove('show');
                };
                control.addEventListener('input', clearError);
                control.addEventListener('change', clearError);
            });
        };
        setupControlErrorClearing();

        /**
         * Form Validation
         */
        function validateForm() {
            let isValid = true;

            // Helper function to validate select / text input
            function checkField(fieldId, errorId) {
                const elem = document.getElementById(fieldId);
                const errElem = document.getElementById(errorId);
                if (!elem) return true;

                const val = elem.value.trim();
                if (!val) {
                    elem.classList.add('error');
                    if (errElem) errElem.classList.add('show');
                    return false;
                } else {
                    elem.classList.remove('error');
                    if (errElem) errElem.classList.remove('show');
                    return true;
                }
            }

            // Validate context fields
            if (!checkField('publicationType', 'publicationTypeError')) isValid = false;
            if (!checkField('language', 'languageError')) isValid = false;
            if (!checkField('state', 'stateError')) isValid = false;
            if (!checkField('district', 'districtError')) isValid = false;
            if (!checkField('periodicity', 'periodicityError')) isValid = false;

            // Validate title inputs
            if (titlesListContainer) {
                const titleInputs = titlesListContainer.querySelectorAll('.title-input');
                titleInputs.forEach((input, index) => {
                    const val = input.value.trim();
                    const wrapper = input.closest('.title-input-wrapper');
                    const errElem = wrapper ? wrapper.querySelector('.error-message') : null;

                    if (index === 0 && !val) {
                        // First title is strictly required
                        input.classList.add('error');
                        if (errElem) errElem.classList.add('show');
                        isValid = false;
                    } else if (val) {
                        input.classList.remove('error');
                        if (errElem) errElem.classList.remove('show');
                    } else {
                        input.classList.remove('error');
                        if (errElem) errElem.classList.remove('show');
                    }
                });
            }

            return isValid;
        }

        /**
         * Form Submit Handler
         */
        analyzerForm.addEventListener('submit', (e) => {
            e.preventDefault();

            if (!validateForm()) {
                // Focus first error field
                const firstError = analyzerForm.querySelector('.form-control.error');
                if (firstError) firstError.focus();
                return;
            }

            // Show loading state
            if (submitBtn) {
                submitBtn.classList.add('loading');
                submitBtn.disabled = true;
            }

            // Collect entered data
            const titleInputs = titlesListContainer ? titlesListContainer.querySelectorAll('.title-input') : [];
            const collectedTitles = Array.from(titleInputs)
                .map(input => input.value.trim())
                .filter(val => val.length > 0);

            const payload = {
                publicationType: document.getElementById('publicationType')?.value || '',
                language: document.getElementById('language')?.value || '',
                state: document.getElementById('state')?.value || '',
                district: document.getElementById('district')?.value.trim() || '',
                periodicity: document.getElementById('periodicity')?.value || '',
                proposedTitles: collectedTitles
            };

            fetch('/api/applications', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(errData => {
                        throw new Error(errData.message || 'Failed to submit application.');
                    });
                }
                return response.json();
            })
            .then(data => {
                console.log('TitleVerify AI - Application saved successfully:', data);
                if (submitBtn) {
                    submitBtn.classList.remove('loading');
                    submitBtn.disabled = false;
                }
                const appId = data ? (data.id || data.applicationId) : null;
                if (!appId) {
                    alert('Application submitted, but server did not return a valid Application ID.');
                    return;
                }
                openModal(data);
            })
            .catch(error => {
                console.error('TitleVerify AI - Submission Error:', error);
                if (submitBtn) {
                    submitBtn.classList.remove('loading');
                    submitBtn.disabled = false;
                }
                alert('Submission failed: ' + error.message);
            });
        });

        // Initialize state
        updateTitleRows();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAnalyzer);
    } else {
        initAnalyzer();
    }
})();
