from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    UserProfileViewSet, PatientViewSet,
    TriageSessionViewSet, VaccinationViewSet, ChatMessageViewSet
)

router = DefaultRouter()
router.register(r'users', UserProfileViewSet)
router.register(r'patients', PatientViewSet)
router.register(r'triage', TriageSessionViewSet)
router.register(r'vaccinations', VaccinationViewSet)
router.register(r'chat', ChatMessageViewSet)

urlpatterns = [
    path('', include(router.urls)),
]
