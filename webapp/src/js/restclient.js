/* eslint-disable no-template-curly-in-string */
export const BASE_IMAGE_URL = 'http://localhost:3000/'
export const BASE_MISSIONS_URL = 'http://localhost:3000/missions'
import { showError } from './missions'

export async function postMission(mission) {
  return await fetch(BASE_MISSIONS_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(mission)
  })
    .then((response) => {
      if (!response.ok) {
        showError("Gegevens niet kunnen toevoegen.")
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError("Gegevens niet kunnen ophalen.")
    })
}

export async function deleteMission(id) {
  return await fetch(`${BASE_MISSIONS_URL}/${id}`, {
    method: 'DELETE'
  })
    .then((response) => {
      if (!response.ok) {
        showError("Gegevens niet kunnen verwijderen.")
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError("Gegevens niet kunnen ophalen.")
    })
}

export async function getMission(id) {
  return await fetch(`${BASE_MISSIONS_URL}/${id}`)
    .then((response) => {
      if (!response.ok) {
        showError("Gegevens niet kunnen ophalen.")
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError("Gegevens niet kunnen ophalen.")
    })
}

export async function getMissions() {
  return await fetch(BASE_MISSIONS_URL)
    .then((response) => {
      if (!response.ok) {
        showError("Gegevens niet kunnen ophalen.")
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError("Gegevens niet kunnen ophalen.")
    })
}