import 'bootstrap'
import './js/restclient'
import { initialiseForm, initialiseFirstMission, filter } from './js/missions'
import * as navActions from './js/navActions'
import { checkFields } from './js/formActions'

import { BASE_IMAGE_URL, BASE_MISSIONS_URL } from './js/restclient'

import 'bootstrap/dist/css/bootstrap.css'
import './css/style.scss'
import '@fortawesome/fontawesome-free/css/all.css'


// display settings for the nav
document.querySelector('#new').style.display = "none"
document.querySelector('#search').style.display = "none"

document.querySelector('#navHome').addEventListener('click', navActions.showSectionHome, false)
document.querySelector('#navLogo').addEventListener('click', navActions.showSectionHome, false)
document.querySelector('#navNew').addEventListener('click', navActions.showSectionNew, false)
document.querySelector('#navSearch').addEventListener('click', navActions.showSectionSearch, false)

init()

// GET logo for the site
const navLogo = document.querySelector('#navLogo');

navLogo.src = `${BASE_IMAGE_URL}soyuz_tma_15_mission_patch.png`

async function init() {
    await initialiseFirstMission()
    await initialiseForm()
}

// form actions
document.querySelector('#addMission').addEventListener('submit', checkFields, false)
document.querySelector('#filter').addEventListener('input', filter, false)
document.querySelector('#matchCase').addEventListener('input', filter, false)