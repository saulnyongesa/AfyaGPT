from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    HealthFacilityViewSet, UserProfileViewSet, PatientViewSet,
    TriageSessionViewSet, VaccinationViewSet, ChatMessageViewSet,
    ClinicalFeedbackViewSet, AuditLogViewSet, NewsArticleViewSet,
    ContactInquiryViewSet, AnnouncementViewSet, AppSettingViewSet,
    LandingSectionViewSet, StakeholderViewSet,
    MobileAuthCheckView, MobileLoginView, MobileRegisterView, BatchSyncView,
    export_patients_csv, export_triage_csv, export_hmis_indicators_csv
)

router = DefaultRouter()
router.register(r'facilities', HealthFacilityViewSet)
router.register(r'users', UserProfileViewSet)
router.register(r'patients', PatientViewSet)
router.register(r'triage', TriageSessionViewSet)
router.register(r'vaccinations', VaccinationViewSet)
router.register(r'chat', ChatMessageViewSet)
router.register(r'feedback', ClinicalFeedbackViewSet)
router.register(r'audit-logs', AuditLogViewSet)
router.register(r'news', NewsArticleViewSet)
router.register(r'contact-inquiries', ContactInquiryViewSet)
router.register(r'announcements', AnnouncementViewSet)
router.register(r'settings', AppSettingViewSet)
router.register(r'landing-sections', LandingSectionViewSet)
router.register(r'stakeholders', StakeholderViewSet)

urlpatterns = [
    path('auth/login/', MobileLoginView.as_view(), name='auth_login'),
    path('auth/register/', MobileRegisterView.as_view(), name='auth_register'),
    path('auth/check/', MobileAuthCheckView.as_view(), name='auth_check'),
    path('sync/', BatchSyncView.as_view(), name='batch_sync'),
    path('export/patients/csv/', export_patients_csv, name='export_patients_csv'),
    path('export/triage/csv/', export_triage_csv, name='export_triage_csv'),
    path('export/hmis/csv/', export_hmis_indicators_csv, name='export_hmis_csv'),
    path('', include(router.urls)),
]
