from rest_framework import serializers
from .models import UserProfile, Patient, TriageSession, Vaccination, ChatMessage

class UserProfileSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserProfile
        fields = '__all__'

class PatientSerializer(serializers.ModelSerializer):
    class Meta:
        model = Patient
        fields = '__all__'

class TriageSessionSerializer(serializers.ModelSerializer):
    class Meta:
        model = TriageSession
        fields = '__all__'

class VaccinationSerializer(serializers.ModelSerializer):
    class Meta:
        model = Vaccination
        fields = '__all__'

class ChatMessageSerializer(serializers.ModelSerializer):
    class Meta:
        model = ChatMessage
        fields = '__all__'
