/* eslint-disable no-template-curly-in-string */
export const NAMING_SERVER_URL = 'http://localhost:5051/api/naming'
import { showError } from './nodes'

// the uri to port translation since we're using ssh tunneling
let nodesServerPort = {"host0.group5.6dist": "5051", "host1.group5.6dist": "5052", "host2.group5.6dist": "5053", "host3.group5.6dist": "5054", "host4.group5.6dist": "5055", "host5.group5.6dist": "5056"}

export async function postNode(node) {
  return await fetch(NAMING_SERVER_URL, {
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
  return await fetch(`${NAMING_SERVER_URL}/${id}`, {
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
  return await fetch(`${NAMING_SERVER_URL}/${id}`)
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
  return await fetch(`${NAMING_SERVER_URL}/nodes/all`)
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

export async function getLocalFilesFromNode(nodeUri) {
  let port = nodesServerPort[nodeUri]

  return await fetch(`http://localhost:${port}/api/files/local/all`)
    .then((response) => {
      if (!response.ok) {
        showError(`Unable to access local files from node ${id}.`)
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError(`Unable to access node ${id}.`)
    })
}

export async function getReplicatedFilesFromNode(nodeUri) {
  let port = nodesServerPort[nodeUri]

  return await fetch(`http://localhost:${port}/api/files/replicated/all`)
    .then((response) => {
      if (!response.ok) {
        showError(`Unable to access replicated files from node ${id}.`)
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError(`Unable to access node ${id}.`)
    })
}