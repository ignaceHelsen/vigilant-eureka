/* eslint-disable no-template-curly-in-string */
export const BASE_NODES_URL = 'http://localhost:5051/api/naming'
import { showError } from './nodes'

export async function postNode(node) {
  return await fetch(BASE_NODES_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(node)
  })
    .then((response) => {
      if (!response.ok) {
        showError("Unable to add new node.")
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError("Unable to access server.")
    })
}

export async function deleteNode(id) {
  return await fetch(`${BASE_NODES_URL}/${id}`, {
    method: 'DELETE'
  })
    .then((response) => {
      if (!response.ok) {
        showError("Unable to delete node.")
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError("Unable to access server.")
    })
}

export async function getNode(id) {
  return await fetch(`${BASE_NODES_URL}/${id}`)
    .then((response) => {
      if (!response.ok) {
        showError("Unable to access node.")
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError("Unable to access server.")
    })
}

export async function getNodes() {
  return await fetch(`${BASE_NODES_URL}/nodes/all`)
    .then((response) => {
      if (!response.ok) {
        showError("Unable to access all nodes.")
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError("Unable to access server.")
    })
}