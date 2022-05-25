/* eslint-disable no-template-curly-in-string */
const NAMING_SERVER_URL = 'http://localhost:5051/api/naming'
import { showError } from './nodes'

// the uri to port translation since we're using ssh tunneling
let nodesServerPort = { "host0.group5.6dist": "5051", "host1.group5.6dist": "5052", "host2.group5.6dist": "5053", "host3.group5.6dist": "5054", "host4.group5.6dist": "5055", "host5.group5.6dist": "5056" }

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

  return await fetch(`http://localhost:${port}/api/local/all`)
    .then((response) => {
      if (!response.ok) {
        showError(`Unable to access local files from node ${nodeUri}.`)
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

  return await fetch(`http://localhost:${port}/api/replicated/all`)
    .then((response) => {
      if (!response.ok) {
        showError(`Unable to access replicated files from node ${nodeUri}.`)
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError(`Unable to access node ${id}.`)
    })
}

export async function getConfigFromNode(nodeUri) {
  let port = nodesServerPort[nodeUri]

  return await fetch(`http://localhost:${port}/api/config`)
    .then((response) => {
      if (!response.ok) {
        showError(`Unable to access config from node ${nodeUri}.`)
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError(`Unable to access node ${id}.`)
    })
}

export async function shutdownNode(nodeUri) {
  let port = nodesServerPort[nodeUri]

  return await fetch(`http://localhost:${port}/api/shutdown`, {
    method: 'POST'
  })
    .then((response) => {
      if (!response.ok) {
        showError("Unable to shutdown node.")
      }
      return response.json()
    })
    .catch((e) => {
      console.error(e)
      showError("Unable to access server.")
    })
}