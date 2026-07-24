from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    UserProfileViewSet, PatientViewSet,
    TriageSessionViewSet, VaccinationViewSet, ChatMessageViewSet,
    NewsArticleViewSet, ContactInquiryViewSet,
    AuthRegisterView, AuthLoginView, BatchSyncView
)

router = DefaultRouter()
router.register(r'users', UserProfileViewSet)
router.register(r'patients', PatientViewSet)
router.register(r'triage', TriageSessionViewSet)
router.register(r'vaccinations', VaccinationViewSet)
router.register(r'chat', ChatMessageViewSet)
router.register(r'news', NewsArticleViewSet)
router.register(r'contact-inquiries', ContactInquiryViewSet)

urlpatterns = [
    path('auth/register/', AuthRegisterView.as_view(), name='auth_register'),
    path('auth/login/', AuthLoginView.as_view(), name='auth_login'),
    path('sync/', BatchSyncView.as_view(), name='batch_sync'),
    path('', include(router.urls)),
]
