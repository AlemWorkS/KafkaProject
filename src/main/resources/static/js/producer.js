    document.addEventListener('DOMContentLoaded', function () {
        const connectButton = document.getElementById('connectButton');
        const serverAddressInput = document.getElementById('serverAddress');
        const statusMessage = document.getElementById('status-message');
        const existingTopicSelect = document.getElementById('existingTopic');
        const newTopicInput = document.getElementById('newTopic');
        const form = document.getElementById('producer-form');
        const messageInput = document.getElementById('message');
        const linkInput = document.getElementById('link');
        const titreInput = document.getElementById('titre');


        let serverAddress = '';
        let isConnected = false;

        // Gestion de la connexion
            fetch('/producer/connect-publisher', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
            })
                .then((response) => response.text())
                .then((data) => {
                    if (data.includes('Connecté')) {
                        statusMessage.textContent = `Connecté`;
                        isConnected = true;
                        loadExistingTopics(); // Charger les topics une fois connecté
                    } else {
                        statusMessage.textContent = 'Non connecté';
                        isConnected = false;
                    }
                })
                .catch((error) => {
                    console.error('Erreur de connexion :', error);
                    statusMessage.textContent = 'Non connecté';
                });

        // Charger les topics existants
        function loadExistingTopics() {
            if (!isConnected /*|| !serverAddress*/) {
                console.error('Serveur non connecté.');
                return;
            }

            fetch(`/producer/list-topics`, {
                method: 'GET',
            })
                .then((response) => {
                    if (!response.ok) {
                        throw new Error('Erreur lors de la récupération des topics.');
                    }
                    return response.json();
                })
                .then((topics) => {
                    // Vider la liste déroulante avant de la remplir
                    existingTopicSelect.innerHTML =
                        '<option value="">Sélectionnez un topic existant</option>';
                    topics.forEach((topic) => {
                        const option = document.createElement('option');
                        option.value = topic;
                        option.textContent = topic;
                        existingTopicSelect.appendChild(option);
                    });
                })
                .catch((error) => {
                    console.error('Erreur lors de la récupération des topics :', error);
                    alert('Impossible de charger les topics. Veuillez réessayer.');
                });
        }

        // Désactiver l'autre champ si l'un est rempli
        existingTopicSelect.addEventListener('change', function () {
            if (existingTopicSelect.value) {
                newTopicInput.disabled = true;
                newTopicInput.value = '';
            } else {
                newTopicInput.disabled = false;
            }
        });

        newTopicInput.addEventListener('input', function () {
            if (newTopicInput.value.trim()) {
                existingTopicSelect.disabled = true;
            } else {
                existingTopicSelect.disabled = false;
            }
        });

        // Validation du formulaire avant l'envoi
        form.addEventListener('submit', function (event) {
            event.preventDefault();

            const selectedTopic = existingTopicSelect.value;
            const newTopic = newTopicInput.value.trim();
            const message = messageInput.value.trim();
            const link = linkInput.value.trim();
            const titre = titreInput.value.trim();


            if (!message && !link) {
                alert('Veuillez entrer un message ou un lien.');
                return;
            }

            if (message && link) {
                alert('Vous ne pouvez pas entrer un message et un lien en même temps.');
                return;
            }

            if (!selectedTopic && !newTopic) {
                alert('Veuillez sélectionner un topic existant ou en créer un nouveau.');
                return;
            }

            const topicName = selectedTopic || newTopic;

            // Préparation des données pour l'envoi
            const data = new URLSearchParams({
                topicName: topicName,
                message: message || '',
                link: link || '',
                titre: titre || '',
            });

            fetch('/producer/send-message', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: data,
            })
                .then((response) => {
                    if (!response.ok) {
                        return response.text().then((err) => {
                            throw new Error(err);
                        });
                    }
                    return response.text();
                })
                .then((result) => {
                    alert(result);
                    form.reset();
                    existingTopicSelect.disabled = false;
                    newTopicInput.disabled = false;
                })
                .catch((error) => {
                    console.error('Erreur lors de l\'envoi du message :', error);
                    alert('Échec de l\'envoi du message.', error);
                });
        });
    });