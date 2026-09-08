/**
 * TitleVerify AI - Detailed Analysis Page Client Logic
 */
function switchTitleTab(selectedCard) {
    if (!selectedCard) return;

    const targetId = selectedCard.getAttribute('data-target');
    if (!targetId) return;

    // Remove active class and aria-selected from all title cards
    const allCards = document.querySelectorAll('.title-card');
    allCards.forEach(card => {
        card.classList.remove('active');
        card.setAttribute('aria-selected', 'false');
    });

    // Add active class and aria-selected to clicked card
    selectedCard.classList.add('active');
    selectedCard.setAttribute('aria-selected', 'true');

    // Hide all detail panels
    const allPanels = document.querySelectorAll('.detail-panel');
    allPanels.forEach(panel => {
        panel.style.display = 'none';
    });

    // Show target detail panel
    const targetPanel = document.getElementById(targetId);
    if (targetPanel) {
        targetPanel.style.display = 'flex';
        targetPanel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

/**
 * Keyboard Navigation for Title Selector Tabs
 */
function handleTabKeydown(event, selectedCard) {
    if (!event || !selectedCard) return;

    const key = event.key;
    if (key === 'Enter' || key === ' ') {
        event.preventDefault();
        switchTitleTab(selectedCard);
    } else if (key === 'ArrowRight' || key === 'ArrowDown') {
        event.preventDefault();
        const nextCard = selectedCard.nextElementSibling;
        if (nextCard && nextCard.classList.contains('title-card')) {
            nextCard.focus();
            switchTitleTab(nextCard);
        }
    } else if (key === 'ArrowLeft' || key === 'ArrowUp') {
        event.preventDefault();
        const prevCard = selectedCard.previousElementSibling;
        if (prevCard && prevCard.classList.contains('title-card')) {
            prevCard.focus();
            switchTitleTab(prevCard);
        }
    }
}
