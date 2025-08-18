export function getToken() {
  return localStorage.getItem("token");
}

export function requireAuth() {
  const token = getToken();
  if (!token) {
    window.location.href = "login.html";
  }
}

export function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
  window.location.href = "login.html";
}
