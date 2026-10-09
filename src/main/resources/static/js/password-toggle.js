document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-password-toggle]');
    if (!button) return;

    const input = button.parentElement.querySelector('input');
    const visible = input.type === 'password';
    const label = visible ? 'Ocultar senha' : 'Mostrar senha';

    input.type = visible ? 'text' : 'password';
    button.setAttribute('aria-pressed', String(visible));
    button.setAttribute('aria-label', label);
    button.setAttribute('title', label);
    button.querySelector('[data-icon="show"]').classList.toggle('d-none', visible);
    button.querySelector('[data-icon="hide"]').classList.toggle('d-none', !visible);
});

document.addEventListener('submit', (event) => {
    event.target.querySelectorAll('[data-password-toggle]').forEach((button) => {
        const input = button.parentElement.querySelector('input');
        if (input.type === 'text') button.click();
    });
});
