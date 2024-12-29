const container = document.getElementById('container');
const registerBtn = document.getElementById('register');
const loginBtn = document.getElementById('login');

registerBtn.addEventListener('click', () => {
    container.classList.add("active");
});

loginBtn.addEventListener('click', () => {
    container.classList.remove("active");
});
document.getElementById("inscriptionForm").addEventListener("submit", function (event) {
  event.preventDefault();

  const firstName = document.querySelector("input[name='firstName']").value;
  const lastName = document.querySelector("input[name='lastName']").value;
  const userName = document.querySelector("input[name='userName']").value;
  const email = document.querySelector("input[name='email']").value;
  const password = document.querySelector("input[name='password']").value;
  const role = document.querySelector("select[name='role']").value;

  console.log("Payload envoyé :", { firstName, lastName,userName, email, password, role });

  fetch("/inscription", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: `firstName=${encodeURIComponent(firstName)}&lastName=${encodeURIComponent(lastName)}&userName=${encodeURIComponent(userName)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}&role=${encodeURIComponent(role)}`,
  })
    .then((response) => {
      if (response.ok) {
        alert("Inscription réussie !");
        window.location.href = "/";
      } else {
        return response.text().then((text) => {
          throw new Error(text);
        });
      }
    })
    .catch((error) => {
      console.error("Erreur :", error);
      alert("Erreur : " + error.message);
    });
});

document.getElementById("loginForm").addEventListener("submit", function (event) {
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
            return response.text(); // Récupérer l'URL de redirection
        } else {
            return response.text().then((text) => {
                throw new Error(text);
            });
        }
    })
    .then((redirectUrl) => {
        window.location.href = redirectUrl; // Redirige vers la page appropriée
    })
    .catch((error) => {
        console.error("Erreur lors de la connexion :", error.message);
        alert("Erreur : " + error.message);
    });
});

