
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
    });



document.getElementById("searchButton").addEventListener("click", function () {
    const interest = document.getElementById("interestInput").value;
    searchMessages(interest);
});

document.getElementById("checkBegining").addEventListener("change", function () {
    const interest = document.getElementById("interestInput").value;
    searchMessages(interest);

});

function searchMessages(interest){



var url = `/search-topics?interest=${interest}&fromBeginning=false`;
if (document.getElementById("checkBegining").checked) {
    url = `/search-topics?interest=${interest}&fromBeginning=true`;
}

    if (interest) {
    console.log(interest)
            fetch(url)
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

}

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
// Fonction pour charger les topics auxquels un utilisateur est abonné
/*document.addEventListener('DOMContentLoaded', () => {
    const userEmail = document.getElementById('userEmail').value; // Récupère la valeur de l'email
    if (userEmail) {
        console.log(`Utilisateur connecté, email détecté : ${userEmail}`);
        loadUserTopics(userEmail); // Appelle la fonction avec l'email récupéré
    } else {
        console.error("Aucun utilisateur connecté : l'email est manquant.");
    }
});

function loadUserTopics() {
    // Récupérer l'email de l'utilisateur depuis le champ caché
    const userEmail = sessionStorage.getItem("userEmail");

    console.log("Chargement des topics pour l'utilisateur :", userEmail);

    if (userEmail) {
        // Appel à l'API backend pour récupérer les topics
        fetch(`subscriptions/topics?userEmail=${encodeURIComponent(userEmail)}`)
            .then(response => {
                console.log("Réponse du serveur reçue :", response);
                return response.json();
            })
            .then(topics => {
                console.log("Topics récupérés :", topics);

                const topicList = document.getElementById('topic-list');
                topicList.innerHTML = ''; // Vider la liste existante

                // Ajouter chaque topic récupéré à la liste
                topics.forEach(topic => {
                    const listItem = document.createElement('li');
                    listItem.textContent = topic;
                    topicList.appendChild(listItem);
                });
            })
            .catch(error => {
                console.error("Erreur lors du chargement des topics :", error);
            });
    } else {
        console.error("Erreur : l'email de l'utilisateur n'a pas pu être récupéré.");

}*/
