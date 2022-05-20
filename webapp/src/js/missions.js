import * as restClient from './restclient'
import { BASE_IMAGE_URL, BASE_MISSIONS_URL } from './restclient'
import { Mission } from './Mission'

export async function initialiseFirstMission() {
    try {
        let mission = await restClient.getMission(1)
        mission = new Mission(mission)
        showMission(mission)
    }
    catch (e) {
        console.log(e)
    }
}

export async function initialiseForm() {
    try {
        let missions = await restClient.getMissions()

        missions = missions.map((obj) => new Mission(obj))
        showMissions(missions)
        showMissionsSearch(missions)
    }
    catch (error) {
        showError('Missies niet gevonden.')
    }
}

function showMission(mission) {
    if (mission != undefined) {
        const formMission = document.querySelector("#formMission");
        formMission.innerHTML = ''
        formMission.innerHTML +=
            `
            <div class="row">
                <div class="col-md-3 col-sm-12 text-center">
                    <img src="${BASE_IMAGE_URL}${mission.logo}" class="missionLogo" alt="mission emblem">
                </div>
                <div class="col-md-3 col-sm-12 text-center">            
                        <h2>${mission.name}</h2>
                </div>
                <div class="col-md-2 col-sm-12 text-center">
                    <p>Crew ${mission.crewSize}</p>
                </div>
                <div class="col-md-2 col-sm-12 text-center">
                    <p>Launch ${mission.launchDate}</p>
                </div>
                <div class="col-md-2 col-sm-12 text-center">
                    <p>${mission.operator}</p>
                </div >
            </div >
            `
    }
}

function showMissions(missions) {
    for (let i = 0; i < missions.length; i++) {
        addMission(missions[i])
    }

    initialiseEventListeners()
}

function addMission(mission) {
    const formMissions = document.querySelector('#formMissions');
    formMissions.innerHTML +=
        `
        <div class="card bg-dark col-sm-6 col-lg-3 col-xl-4 mb-5 mt-5">
            <div class="row">
            <div class="col-6">
                <img id="${mission.id}" src="${BASE_IMAGE_URL}${mission.logo}" class="missionLogo mt-2 float-right" alt="mission emblem">
            </div>
            <div class="col-6">
                <h3 class="mt-2">${mission.name}</h2>
                <p>${mission.crewSize}</p>
                <p>${mission.launchDate}</p>
                <p>${mission.operator}</p>
            </div>
            </div>
        </div>
      `
}

export function initialiseEventListeners() {
    const images = document.getElementsByClassName('missionLogo');

    for (let i = 0; i < images.length; i++) {
        images[i].addEventListener('click', showMissionById, false);
    }
}

async function showMissionById(event) {
    const id = (event.target.id)

    try {
        let mission = await restClient.getMission(id)
        mission = new Mission(mission)

        showMission(mission)
    }
    catch (e) {
        showError('Missie niet gevonden.')
    }
}

export async function filter(event) {
    let text = document.querySelector('#filter').value
    let matchCase = document.querySelector('#matchCase')

    try {
        let missions = await restClient.getMissions()
        missions = missions.map((obj) => new Mission(obj))
        let missionList

        if (text != null) {
            if (matchCase.checked) {
                missionList = missions.filter(m => m.name.includes(text) || m.operator.includes(text))
            }
            else {
                missionList = missions.filter(m => m.name.toLowerCase().includes(text.toLowerCase()) || m.operator.toLowerCase().includes(text.toLowerCase()))
            }
        }

        showMissionsSearch(missionList)
    }
    catch (e) {
        showError('Missies niet gevonden.')
    }
}



function showMissionsSearch(missions) {
    const tableSearch = document.querySelector('#tableSearch');
    const tbody = tableSearch.querySelector('tbody')
    tbody.innerHTML = ''
    for (let i = 0; i < missions.length; i++) {
        addMissionToTable(missions[i])
    }

    document.querySelector('#count').innerHTML = missions.length
}

function addMissionToTable(mission) {
    const tableSearch = document.querySelector('#tableSearch');
    const tbody = tableSearch.querySelector('tbody')
    tbody.innerHTML +=
        `<tr>
            <th scope="row">${mission.id}</th>
            <td><img src="${BASE_IMAGE_URL}${mission.logo}" class="missionLogoTable" alt="mission emblem"></td>
            <td>${mission.name}</td>
            <td>${mission.crewSize}</td>
            <td>${mission.launchDate}</td>
            <td>${mission.operator}</td>
        </tr>
        `
}


export function showError(text) {
    const notFound = document.querySelector('#notFound');
    notFound.innerHTML = text
}