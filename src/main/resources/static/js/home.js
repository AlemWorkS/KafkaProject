//Block afficahge list de topics sur le home
document.addEventListener('DOMContentLoaded', function () {

const topicList = document.getElementById('topicList');

fetch('/list-topics')
    .then(reponses => reponses.json())
    .then(topics => {
        topics.forEach(topic => {
            console.log(topic);
                const listItem = document.createElement("li");
                listItem.textContent = topic;
                listItem.addEventListener("click", function() {
                    searchMessages(topic);
                    document.getElementById("interestInput").value = topic;  // Corrected here
                });
                topicList.appendChild(listItem);
            })
        }).catch(error => {
            console.error("Erreur lors de la recherche :", error);
        });

 const planningForm = document.getElementById("planning_form");

    if (planningForm) {
        planningForm.addEventListener("submit", function (event) {
            event.preventDefault(); // Empêche le rechargement de la page

            // Récupération des valeurs du formulaire
            let formData = new FormData(planningForm);

            // Envoi des données via Fetch API
            fetch("/make-planning", {
                method: "POST",
                body: formData
            })
            .then(response => response.text()) // Convertir la réponse en texte
            .then(data => {
                alert(data); // Affiche la réponse du serveur dans une popup
            })
            .catch(error => {
                console.error("Erreur lors de la soumission :", error);
                alert("Une erreur est survenue. Veuillez réessayer.");
            });
        });
    }
    });

//Fin block affichage des topics

//Block button de recherches des messages topics

document.getElementById("searchButton").addEventListener("click", function () {
    const interest = document.getElementById("interestInput").value;
    searchMessages(interest);
});

document.getElementById("checkBegining").addEventListener("change", function () {
    const interest = document.getElementById("interestInput").value;
    searchMessages(interest);

});

//Fin block des buttons de recherche des messages topics

//Block fonction de recherche des messages topics

function searchMessages(interest) {
    const checkBox = document.getElementById("checkBegining");
    // Ici, nous interprétons la checkbox comme "Afficher les messages lus".
    // Si cochée, on ne doit afficher que les messages lus.
    const showReadMessages = checkBox.checked;

    // On lit toujours tous les messages depuis le début pour ne pas perdre les messages non encore lus.
    let url = `/search-topics?interest=${interest}&fromBeginning=true`;

    if (interest) {
        fetch(url)
            .then(response => response.json())
            .then(topics => {
                const alertList = document.querySelector(".alert-list");
                alertList.innerHTML = ""; // Vider la liste actuelle

                // Récupérer la liste des messages lus depuis le localStorage
                // Ce tableau contient des identifiants de messages au format "topic-offset"
                let readMessages = JSON.parse(localStorage.getItem('readMessages')) || [];

                Object.keys(topics).forEach(key => {
                    const data = topics[key];
                    // Construction de l'identifiant unique du message (exemple: "mytopic-123")
                    const messageId = data.topic + '-' + data.offset;

                    // Filtrage en fonction de l'état de la checkbox
                    if (showReadMessages) {
                        // Si la checkbox est cochée, on n'affiche que les messages lus.
                        if (!readMessages.includes(messageId)) {
                            return; // Le message n'est pas marqué comme lu : on le saute
                        }
                    } else {
                        // Si la checkbox n'est pas cochée, on n'affiche que les messages non lus.
                        if (readMessages.includes(messageId)) {
                            return; // Le message est déjà lu : on le saute
                        }
                    }

                    // Créer la carte pour afficher le message
                    const card = document.createElement("div");
                    card.classList.add("alert-card");

                    // Vérifier si le message est vide ou indique "Aucun Nouveau Message"
                    const isMessageEmpty = data.message === "" || data.message.includes("Aucun Nouveau Message");
                    const messageContent = isMessageEmpty
                        ? `<p>Message : Aucun Nouveau Message sur le topic ${data.theme}</p>`
                        : `<p><span>Message : </span>${data.message.substring(0, 50)}...</p>`;

                    // Générer le bouton "Voir Plus" uniquement si le message n'est pas vide
                    // On y ajoute les attributs data pour stocker le topic et l'offset
                    const seeMoreButton = !isMessageEmpty
                        ? `<a href="/full-message?title=${encodeURIComponent(data.theme)}&content=${encodeURIComponent(data.message)}"
                                class="btn-see-more"
                                data-topic="${data.topic}"
                                data-offset="${data.offset}">
                                Voir Plus
                           </a>`
                        : "";

                    card.innerHTML = `
                        <div class="alert-card-header">
                            <h2>Thème : ${data.theme}</h2>
                        </div>
                        ${messageContent}
                        ${seeMoreButton}
                    `;

                    alertList.appendChild(card);
                });

                // Attacher l'événement sur les boutons "Voir Plus"
                document.querySelectorAll('.btn-see-more').forEach(button => {
                    button.addEventListener('click', function (e) {
                        e.preventDefault(); // Empêche la redirection immédiate

                        // Récupérer les informations du message via les attributs data
                        const topic = this.getAttribute('data-topic');
                        const offset = this.getAttribute('data-offset');
                        const messageId = topic + '-' + offset;

                        // Récupérer ou initialiser la liste des messages lus dans le localStorage
                        let readMessages = JSON.parse(localStorage.getItem('readMessages')) || [];
                        if (!readMessages.includes(messageId)) {
                            readMessages.push(messageId);
                        }
                        localStorage.setItem('readMessages', JSON.stringify(readMessages));

                        // Rediriger vers la page de détail du message
                        window.location.href = this.href;
                    });
                });
            })
            .catch(error => {
                console.error("Erreur lors de la recherche :", error);
            });
    } else {
        alert("Veuillez entrer un centre d'intérêt !");
    }
}




//Fin block fonction de recherche des messages topics

//Block gestion de souscription

//Block attribution de la fonction au boutton de souscription
function attachSubscribeEventHandlers() {
    const subscribeButtons = document.querySelectorAll(".subscribe-button");

    subscribeButtons.forEach(button => {
        button.addEventListener("click", function () {
            const topicName = this.dataset.topic;
            subscribe(topicName);
        });
    });
}

//Fin block d'attribution de la fonction au button de souscription

//Fonction de souscription
fetch("/current-user-email")
    .then((response) => response.text())
    .then((email) => {
        sessionStorage.setItem("userConsumerEmail", email);
        console.log("Email utilisateur récupéré :", email);
    })
    .catch((error) => console.error("Erreur lors de la récupération de l'email :", error));

document.getElementById("subscribeButton").addEventListener("click", function () {
    const topicName = document.getElementById("interestInput").value.trim(); // Récupère et nettoie la valeur
    const userEmail = sessionStorage.getItem("userConsumerEmail"); // Récupère l'email de la session

    if (!topicName || !userEmail) {
        alert("Veuillez entrer un centre d'intérêt et vérifier votre connexion.");
        return;
    }


    fetch("/subscribe-to-topic", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({
            email: userEmail,
            topicName: topicName,
        }),
    })
        .then((response) => {
            if (response.ok) {
                return response.text();
            } else {
                return response.text().then((text) => {
                    throw new Error(text);
                });
            }
        })
        .then((message) => alert(message))
        .catch((error) => console.error("Erreur lors de l'abonnement :", error));
});


function subscribe(topicName) {


    const userEmail = sessionStorage.getItem("userConsumerEmail");
    console.log(userEmail);

    if (!userEmail) {
        alert("Erreur : utilisateur non identifié !");
        return;
    }


    fetch(`/subscriptions/subscribe`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            topicName: topicName,
            userEmail: userEmail
        })
    })
        .then(response => response.text())
        .then(message => {
            alert(message); // Affiche le message de succès ou d'erreur
        })
        .catch(error => {
            alert("Erreur lors de l'abonnement : " + error);
        });
}
//Fin block fonction de souscription
//Fin block gestion de la souscription

