import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token'); // 🔑 Récupère le token JWT

  if (token) {
    const clonedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`, // 🔐 Ajoute le token dans les en-têtes
      },
    });
    return next(clonedReq);
  }

  return next(req);
};
