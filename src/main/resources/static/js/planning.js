//Ajouter l'envoi du formulaire au click du button pour plannifier
document.getElementById('planning_form').addEventListener('submit',function(event){

    //Récupérer les informations du formulaire de planning
    const heure = document.getElementById('planning_heure').value.trim();
    const interval = document.getElementById('planning_interval').value.trim();

    console.log(heure);
    console.log(interval);
    //Envoie les informations
    fetch("/make-planning", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `planning_heure=${encodeURIComponent(heure)}}&planning_interval=${encodeURIComponent(interval)}`
        })
        .then(message =>{alert(message)})
        .catch(error => {
                alert("Erreur lors de l'enregistrement :", error);
            });
        });