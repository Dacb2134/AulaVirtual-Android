package com.practicas.aulavirtualapp.utils

import com.practicas.aulavirtualapp.model.SiteInfoResponse
import com.practicas.aulavirtualapp.model.auth.UserDetail
import com.practicas.aulavirtualapp.model.auth.UserRole

object UserRoleResolver {

    fun resolve(siteInfo: SiteInfoResponse, userDetail: UserDetail?): UserRole {
        if (siteInfo.isSiteAdmin) {
            return UserRole.ADMIN
        }
        val roles = userDetail?.roles.orEmpty()
        val tieneRolDocente = roles.any { rol ->
            rol.shortName == "editingteacher" ||
                rol.shortName == "teacher" ||
                rol.shortName == "coursecreator" ||
                rol.shortName == "manager"
        }
        val esDepartamentoDocente = userDetail?.department?.contains("Docente", ignoreCase = true) == true
        return if (tieneRolDocente || esDepartamentoDocente) {
            UserRole.DOCENTE
        } else {
            UserRole.ESTUDIANTE
        }
    }
}
