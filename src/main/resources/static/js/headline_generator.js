document.addEventListener('DOMContentLoaded', function () {
    const headlineForm = document.getElementById('headlineForm');
    const articleContent = document.getElementById('articleContent');
    const contentCounter = document.getElementById('contentCounter');
    const contentError = document.getElementById('contentError');

    const publicationTypeSelect = document.getElementById('publicationType');
    const languageSelect = document.getElementById('language');
    const stateSelect = document.getElementById('state');
    const districtInput = document.getElementById('district');
    const periodicitySelect = document.getElementById('periodicity');

    const generateBtn = document.getElementById('generateBtn');
    const btnSpinner = document.getElementById('btnSpinner');
    const btnText = document.getElementById('btnText');

    const progressCard = document.getElementById('progressCard');
    const step1 = document.getElementById('step1');
    const step2 = document.getElementById('step2');
    const step3 = document.getElementById('step3');

    const errorBanner = document.getElementById('errorBanner');
    const errorMessageText = document.getElementById('errorMessageText');

    const resultsWrapper = document.getElementById('resultsWrapper');
    const recHeadlineText = document.getElementById('recHeadlineText');
    const recContentRelevance = document.getElementById('recContentRelevance');
    const recSemanticMatch = document.getElementById('recSemanticMatch');
    const recDecisionBadge = document.getElementById('recDecisionBadge');
    const recRiskScore = document.getElementById('recRiskScore');
    const recWhyText = document.getElementById('recWhyText');
    const generatedLangTag = document.getElementById('generatedLangTag');

    const headlineCardsGrid = document.getElementById('headlineCardsGrid');
    const generateAgainBtn = document.getElementById('generateAgainBtn');

    const MIN_CHARS = 10;
    const MAX_CHARS = 5000;

    // Live Character Counter
    articleContent.addEventListener('input', updateCharCounter);
    updateCharCounter();

    function updateCharCounter() {
        const len = articleContent.value.length;
        contentCounter.textContent = `${len} / ${MAX_CHARS} characters`;

        if (len > 0 && len < MIN_CHARS) {
            contentCounter.className = 'char-counter warning';
            contentError.textContent = `Content is too short (minimum ${MIN_CHARS} characters required).`;
        } else if (len > MAX_CHARS) {
            contentCounter.className = 'char-counter invalid';
            contentError.textContent = `Content exceeds maximum limit of ${MAX_CHARS} characters.`;
        } else {
            contentCounter.className = 'char-counter';
            contentError.textContent = '';
        }
    }

    // Form Submit Handler
    headlineForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const content = articleContent.value.trim();
        if (content.length < MIN_CHARS) {
            contentError.textContent = `Please enter at least ${MIN_CHARS} characters of news content.`;
            articleContent.focus();
            return;
        }

        if (content.length > MAX_CHARS) {
            contentError.textContent = `Content cannot exceed ${MAX_CHARS} characters.`;
            articleContent.focus();
            return;
        }

        contentError.textContent = '';
        hideError();
        resultsWrapper.style.display = 'none';

        setLoading(true);
        showProgressSteps();

        const selectedLang = languageSelect ? languageSelect.value : 'English';
        const payload = {
            content: content,
            publicationType: publicationTypeSelect ? publicationTypeSelect.value : 'Newspaper',
            language: selectedLang,
            state: stateSelect ? stateSelect.value : 'Karnataka',
            district: (districtInput && districtInput.value.trim().length > 0) ? districtInput.value.trim() : 'Bengaluru Urban',
            periodicity: periodicitySelect ? periodicitySelect.value : 'Daily'
        };

        try {
            const response = await fetch('/api/headline-generator/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (!response.ok || data.status === 'ERROR') {
                throw new Error(data.message || 'An error occurred while generating headlines.');
            }

            // Complete progress steps
            setStepState(step3, 'completed');
            await delay(400);
            progressCard.style.display = 'none';

            renderResults(data, selectedLang);
            resultsWrapper.style.display = 'block';
            resultsWrapper.scrollIntoView({ behavior: 'smooth' });

        } catch (err) {
            progressCard.style.display = 'none';
            showError(err.message || 'Failed to connect to the server. Please try again.');
        } finally {
            setLoading(false);
        }
    });

    // Generate Again Handler
    if (generateAgainBtn) {
        generateAgainBtn.addEventListener('click', function () {
            document.getElementById('inputCard').scrollIntoView({ behavior: 'smooth' });
            articleContent.focus();
        });
    }

    function setLoading(isLoading) {
        if (isLoading) {
            generateBtn.disabled = true;
            btnSpinner.style.display = 'inline-block';
            btnText.textContent = 'PROCESSING...';
        } else {
            generateBtn.disabled = false;
            btnSpinner.style.display = 'none';
            btnText.textContent = 'GENERATE & VERIFY';
        }
    }

    function showProgressSteps() {
        progressCard.style.display = 'block';
        setStepState(step1, 'active');
        setStepState(step2, 'inactive');
        setStepState(step3, 'inactive');

        setTimeout(() => {
            setStepState(step1, 'completed');
            setStepState(step2, 'active');
        }, 500);

        setTimeout(() => {
            setStepState(step2, 'completed');
            setStepState(step3, 'active');
        }, 1100);
    }

    function setStepState(stepElem, state) {
        stepElem.classList.remove('active', 'completed');
        if (state === 'active') {
            stepElem.classList.add('active');
        } else if (state === 'completed') {
            stepElem.classList.add('completed');
        }
    }

    function renderResults(data, selectedLang) {
        if (generatedLangTag) {
            generatedLangTag.textContent = `Generated in: ${selectedLang || 'English'}`;
        }

        // Render Recommended Headline
        const rec = data.recommendedHeadline;
        if (rec) {
            recHeadlineText.textContent = rec.generatedHeadline;
            recContentRelevance.textContent = `${rec.contentRelevance.toFixed(1)}%`;

            recSemanticMatch.textContent = formatSemanticMatchDisplay(rec.verificationResult);

            if (rec.verificationResult) {
                const decision = rec.verificationResult.finalDecision || 'UNKNOWN';
                recDecisionBadge.textContent = decision;
                recDecisionBadge.className = 'badge-decision ' + getDecisionClass(decision);
                recRiskScore.textContent = (rec.verificationResult.riskScore != null)
                    ? rec.verificationResult.riskScore.toFixed(1)
                    : 'N/A';
            } else {
                recDecisionBadge.textContent = 'UNVERIFIED';
                recDecisionBadge.className = 'badge-decision review';
                recRiskScore.textContent = 'N/A';
            }

            recWhyText.textContent = data.recommendationReason || 'Pre-screening recommendation based on story alignment and title clearance safety.';
        }

        // Render 5 Headline Cards
        headlineCardsGrid.innerHTML = '';
        if (data.results && data.results.length > 0) {
            data.results.forEach((item, index) => {
                const card = createHeadlineCard(item, index + 1);
                headlineCardsGrid.appendChild(card);
            });
        }
    }

    function createHeadlineCard(item, index) {
        const card = document.createElement('div');
        card.className = 'headline-result-card';

        const v = item.verificationResult || {};
        // Backend single source of truth for decision, risk score, risk level, confidence
        const decision = v.finalDecision || (item.verificationError ? 'UNAVAILABLE' : 'N/A');
        const decisionClass = getDecisionClass(decision);
        const riskLevel = v.riskLevel || (item.verificationError ? 'UNAVAILABLE' : 'LOW RISK');
        const riskScoreText = v.riskScore != null ? `${v.riskScore.toFixed(1)} / 100` : 'N/A';
        const confidence = v.approvalConfidence != null ? `${v.approvalConfidence.toFixed(1)}%` : 'N/A';

        // Semantic Match state formatting (Case A vs Case B vs Case C)
        const semanticMatch = formatSemanticMatchDisplay(v);
        const closestMatch = v.closestMatch || 'No comparable registered title found';
        const recommendation = v.recommendation || 'Title demonstrates low verification risk under automated pre-screening criteria.';

        let reasonsHtml = '';
        if (v.reasons && v.reasons.length > 0) {
            reasonsHtml = v.reasons.map(r => `<li>${escapeHtml(r)}</li>`).join('');
        } else {
            reasonsHtml = '<li>No specific rule findings or similarity conflicts reported.</li>';
        }

        card.innerHTML = `
            <div class="card-headline-header">
                <span class="headline-num-badge">#${index}</span>
                <div class="headline-text-content">${escapeHtml(item.generatedHeadline)}</div>
                <span class="badge-decision ${decisionClass}">${escapeHtml(decision)}</span>
            </div>

            <div class="headline-metrics-row">
                <div class="h-metric">
                    <span class="h-label">Content Relevance</span>
                    <span class="h-val val-relevance">${item.contentRelevance.toFixed(1)}%</span>
                </div>
                <div class="h-metric">
                    <span class="h-label">Semantic Match</span>
                    <span class="h-val">${escapeHtml(semanticMatch)}</span>
                </div>
                <div class="h-metric">
                    <span class="h-label">Risk Level</span>
                    <span class="h-val">${escapeHtml(riskLevel)}</span>
                </div>
                <div class="h-metric">
                    <span class="h-label">Risk Score</span>
                    <span class="h-val">${escapeHtml(riskScoreText)}</span>
                </div>
                <div class="h-metric">
                    <span class="h-label">Estimated Confidence</span>
                    <span class="h-val">${escapeHtml(confidence)}</span>
                </div>
            </div>

            <div class="closest-match-content" style="margin-bottom: 0.75rem;">
                <strong>Closest Existing Registered Title:</strong> ${escapeHtml(closestMatch)}
            </div>

            <div style="font-size: 0.85rem; color: #475569; margin-bottom: 0.5rem;">
                <strong>Why this result:</strong>
                <ul class="reasons-list" style="margin-top: 0.25rem;">${reasonsHtml}</ul>
            </div>

            <div style="font-size: 0.85rem; color: #15803d; background: #f0fdf4; padding: 0.6rem 0.8rem; border-radius: 6px; border-left: 3px solid #16a34a;">
                <strong>Recommendation:</strong> ${escapeHtml(recommendation)}
            </div>
        `;

        return card;
    }

    /**
     * Formats semantic match state distinguishing:
     * CASE A: No candidate titles available for comparison -> "No comparable registered title found"
     * CASE B: Candidates exist but semantic vector analysis unavailable -> "Semantic analysis unavailable"
     * CASE C: Real semantic similarity value present -> "XX.X%"
     */
    function formatSemanticMatchDisplay(v) {
        if (!v) return 'Verification unavailable';
        if (!v.candidateMatches || v.candidateMatches.length === 0) {
            return 'No comparable registered title found';
        }
        if (v.highestSemanticSimilarity == null) {
            return 'Semantic analysis unavailable';
        }
        return `${(v.highestSemanticSimilarity * 100).toFixed(1)}%`;
    }

    function getDecisionClass(decision) {
        if (!decision) return 'review';
        const d = decision.toUpperCase();
        if (d === 'ACCEPT') return 'accept';
        if (d === 'REVIEW') return 'review';
        if (d === 'HIGH RISK' || d === 'HIGH_RISK') return 'high-risk';
        return 'review';
    }

    function showError(msg) {
        errorMessageText.textContent = msg;
        errorBanner.style.display = 'flex';
        errorBanner.scrollIntoView({ behavior: 'smooth' });
    }

    function hideError() {
        errorBanner.style.display = 'none';
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
});
