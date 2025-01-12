document.addEventListener('DOMContentLoaded', function () {
    const connectButton = document.getElementById('connectButton');
    const serverAddressInput = document.getElementById('serverAddress');
    const serverAddressHiddenInput = document.getElementById('serverAddressInput');
    const createTopicButton = document.getElementById('createTopicButton');
    let serverAddress = '';



    // Gérer la connexion au serveur Kafka
    connectButton.addEventListener('click', function () {
        serverAddress = 'localhost:29092,localhost:39092,localhost:49092';
        if (serverAddress) {
            fetch('/connect-publisher', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            })
                .then(response => response.text())
                .then(data => {
                    document.getElementById('status-message').innerText = data.includes('Connecté') ? 'Statut : Connecté' : 'Statut : Non connecté';
                    if (data.includes('Connecté')) {
                        serverAddressHiddenInput.value = serverAddress; // Mettre à jour l'adresse du serveur pour la création de topic
                        loadTopics();
                    }
                })
                .catch(error => {
                    document.getElementById('status-message').innerText = 'Statut : Non connecté';
                });
        }
    });

    // Gérer la création d'un topic
    createTopicButton.addEventListener('click', function () {
        const topicName = document.querySelector("input[name='topicName']").value;
        if (serverAddress && topicName) {
            fetch('/create-topic', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({
                    topicName: topicName                })
            })
                .then(response => response.text())
                .then(data => {
                    if (data.includes('succès')) {
                        alert('Topic créé avec succès');
                        loadTopics(); // Actualiser la liste des topics
                    } else {
                        alert('Erreur: ' + data);
                    }
                })
                .catch(error => alert('Erreur lors de la création du topic : ' + error));
        } else {
            alert("Veuillez vous connecter au serveur avant de créer un topic");
        }
    });

    // Charger les topics existants
    function loadTopics() {
            fetch(`/list-topics`)
                .then(response => response.json())
                .then(topics => {
                    let topicList = document.getElementById("topic-list");
                    topicList.innerHTML = ""; // Vider la liste existante
                    topics.forEach(topic => {
                        let listItem = document.createElement("li");
                        listItem.textContent = topic;
                        topicList.appendChild(listItem);
                    });
                })
                .catch(error => console.error('Erreur lors de la récupération des topics:', error));
        }
});