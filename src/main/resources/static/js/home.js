document.getElementById("searchButton").addEventListener("click", function () {
    const interest = document.getElementById("interestInput").value;

    if (interest) {
        fetch(`/search-topics?interest=${interest}`)
            .then(response => response.json())
            .then(topics => {

            // Vider les cartes existantes
            const alertList = document.querySelector(".alert-list");
                                                alertList.innerHTML = "";

                Object.keys(topics).forEach(topic=>{
                console.log(topic+"");
                const data = topics[topic];

                                        const card = document.createElement("div");
                                        card.classList.add("alert-card");

                                        // Ajouter la structure HTML de chaque topic
                                        card.innerHTML = `
                                            <div class="alert-card-header">
                                                <h2>Theme : ${data.theme}</h2>

                                            </div>
                                            <p><span>Message : </span><br>${data.message}</p>
                                                                    <button class="subscribe-button" data-topic="${data.topic}">S'abonner</button>

                                        `;
                                                            alertList.appendChild(card);
                                                            attachSubscribeEventHandlers();


                })
            /*
                const alertList = document.querySelector(".alert-list");
                alertList.innerHTML = ""; // Vider les cartes existantes

                topics.forEach(topic => {
                    const card = document.createElement("div");
                    card.classList.add("alert-card");

                    // Ajouter la structure HTML de chaque topic
                    card.innerHTML = `
                        <div class="alert-card-header">
                            <img class="user-icon" src="/images/user.png" alt="Icone utilisateur">
                            <h3>${topic}</h3>
                        </div>
                        <p>Messages : <span id="messages-${topic}">Chargement...</span></p>
                        <button class="subscribe-button" data-topic="${topic}">S'abonner</button>
                    `;

                    alertList.appendChild(card);

                    // Charger automatiquement les messages associés à ce topic
                    fetch(`/get-messages?topicName=${topic}`)
                        .then(res => res.json())
                        .then(messages => {
                            const messageSpan = document.getElementById(`messages-${topic}`);
                            // Vérifier si des messages sont disponibles
                            messageSpan.innerText = messages.length > 0 ? messages.join(", ") : "Aucun message disponible.";
                        })
                        .catch(err => {
                            console.error(`Erreur lors de la récupération des messages pour le topic ${topic} :`, err);
                            const messageSpan = document.getElementById(`messages-${topic}`);
                            messageSpan.innerText = "2 Erreur lors du chargement des messages.";
                        });
                });

                // Attacher les gestionnaires d'événements après avoir ajouté les boutons
                */
            })
            .catch(error => {
                console.error("1 Erreur lors de la recherche :", error);
            });
    } else {
        alert("Veuillez entrer un centre d'intérêt !");
    }
});

function attachSubscribeEventHandlers() {
    const subscribeButtons = document.querySelectorAll(".subscribe-button");

    subscribeButtons.forEach(button => {
        button.addEventListener("click", function () {
            const topicName = this.dataset.topic;
            subscribe(topicName);
        });
    });
}

function subscribe(topicName) {


    const userEmail = sessionStorage.getItem("userEmail");
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

/*// Fonction pour charger les abonnements d'un utilisateur
function updateSubscriptions() {
    const userEmail = document.getElementById("userEmail").value;

    if (!userEmail) {
        console.error("Utilisateur non connecté");
        return;
    }

    fetch(`/subscriptions/user?userEmail=${userEmail}`)
        .then(response => response.json())
        .then(subscriptions => {
            const publishersList = document.querySelector(".sidebar ul");
            publishersList.innerHTML = ""; // Vider la liste actuelle

            subscriptions.forEach(subscription => {
                const li = document.createElement("li");
                li.textContent = subscription;
                publishersList.appendChild(li);
            });
        })
        .catch(error => {
            console.error("Erreur lors de la mise à jour des abonnements :", error);
        });
}
*/