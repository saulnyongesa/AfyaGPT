from rest_framework import viewsets
from .models import UserProfile, Patient, TriageSession, Vaccination, ChatMessage
from .serializers import (
    UserProfileSerializer, PatientSerializer,
    TriageSessionSerializer, VaccinationSerializer, ChatMessageSerializer
)

class UserProfileViewSet(viewsets.ModelViewSet):
    queryset = UserProfile.objects.all().order_by('-created_at')
    serializer_class = UserProfileSerializer

class PatientViewSet(viewsets.ModelViewSet):
    queryset = Patient.objects.all().order_by('-created_at')
    serializer_class = PatientSerializer

class TriageSessionViewSet(viewsets.ModelViewSet):
    queryset = TriageSession.objects.all().order_by('-created_at')
    serializer_class = TriageSessionSerializer

class VaccinationViewSet(viewsets.ModelViewSet):
    queryset = Vaccination.objects.all()
    serializer_class = VaccinationSerializer

class ChatMessageViewSet(viewsets.ModelViewSet):
    queryset = ChatMessage.objects.all().order_by('timestamp')
    serializer_class = ChatMessageSerializer
