const container = document.getElementById('container');
const registerBtn = document.getElementById('register');
const loginBtn = document.getElementById('login');

registerBtn.addEventListener('click', () => {
    container.classList.add("active");
});

loginBtn.addEventListener('click', () => {
    container.classList.remove("active");
});
document.querySelector('#inscription').addEventListener('click', async (e) => {
    e.preventDefault();

    const firstName = document.querySelector('#first-name').value;
    const lastName = document.querySelector('#last-name').value;
    const username = document.querySelector('#username').value;
    const email = document.querySelector('#email').value;
    const password = document.querySelector('#password').value;

    try {
        const response = await fetch('/inscription', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ firstName, lastName, username, email, password }),
        });

        const message = await response.text();

        if (response.ok) {
            alert("Inscription réussie !");
            // Redirection vers la page de connexion
            window.location.href = '/login'; // Modifiez cette URL si nécessaire
        } else {
            alert(`Erreur : ${message}`);
        }
    } catch (error) {
        console.error('Erreur réseau', error);
        alert("Une erreur est survenue. Veuillez réessayer.");
    }
});
document.getElementById("loginForm").addEventListener("submit", function (event) {
  sessionStorage.setItem("userEmail",document.getElementById("email").value);
  event.preventDefault(); // Empêche l'envoi du formulaire par défaut

  const email = document.querySelector("input[name='email']").value;
  const password = document.querySelector("input[name='password']").value;

  fetch("/process-login", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`,
  })
    .then((response) => {
      if (response.ok) {
        window.location.href = "/home";
      } else {
        return response.text().then((text) => alert("Erreur : " + text));
      }
    })
    .catch((error) => console.error("Erreur lors de la connexion :", error));
});


