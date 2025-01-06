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
                                                <div class="alert-card-header" >
                                                    <h2>Theme : ${data.theme}</h2>

                                                </div>
                                                <p><span>Message : </span><br>${data.message}</p>

                                            `;
                                                                alertList.appendChild(card);
                                                                attachSubscribeEventHandlers();


                    })

                })
                .catch(error => {
                    console.error("1 Erreur lors de la recherche :", error);
                });
        } else {
            alert("Veuillez entrer un centre d'intérêt !");
        }

}

function linkCheck(message){

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


//Fonction de souscription
fetch("/current-user-email")
    .then((response) => response.text())
    .then((email) => {
        sessionStorage.setItem("userEmail", email);
        console.log("Email utilisateur récupéré :", email);
    })
    .catch((error) => console.error("Erreur lors de la récupération de l'email :", error));

document.getElementById("subscribeButton").addEventListener("click", function () {
    const topicName = document.getElementById("interestInput").value.trim(); // Récupère et nettoie la valeur
    const userEmail = sessionStorage.getItem("userEmail"); // Récupère l'email de la session

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




//Fin block fonction de souscription
//Fin block gestion de la souscription

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
