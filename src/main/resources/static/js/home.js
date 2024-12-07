document.getElementById("searchButton").addEventListener("click", function () {
    const interest = document.getElementById("interestInput").value;

    if (interest) {
        fetch(`/search-topics?interest=${interest}`)
            .then(response => response.json())
            .then(topics => {
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
                        <button onclick="subscribe('${topic}')">S'abonner</button>
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
                            messageSpan.innerText = "Erreur lors du chargement des messages.";
                        });
                });
            })
            .catch(error => {
                console.error("Erreur lors de la recherche :", error);
            });
    } else {
        alert("Veuillez entrer un centre d'intérêt !");
    }
});

function subscribe(topicName) {
    fetch(`/subscribe`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            topicName: topicName,
            consumerId: "user123" // Remplacez par l'ID réel du consommateur
        })
    })
    .then(response => response.text())
    .then(message => {
        alert(`Abonné au topic : ${topicName}`);
    })
    .catch(error => {
        alert("Erreur lors de l'abonnement : " + error);
    });
}
