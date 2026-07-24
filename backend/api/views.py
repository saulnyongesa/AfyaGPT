from rest_framework import viewsets, status
from rest_framework.views import APIView
from rest_framework.response import Response
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

class AuthRegisterView(APIView):
    """API view for health worker registration & one-time sync."""
    def post(self, request):
        phone_number = request.data.get('phoneNumber') or request.data.get('phone_number')
        if not phone_number:
            return Response({'error': 'Phone number is required'}, status=status.HTTP_400_BAD_REQUEST)
        
        user = UserProfile.objects.filter(phone_number=phone_number).first()
        if user:
            serializer = UserProfileSerializer(user)
            return Response({'status': 'exists', 'user': serializer.data}, status=status.HTTP_200_OK)
        
        serializer = UserProfileSerializer(data={
            'full_name': request.data.get('fullName') or request.data.get('full_name', ''),
            'phone_number': phone_number,
            'email': request.data.get('email'),
            'profession': request.data.get('profession', 'COMMUNITY_HEALTH_WORKER'),
            'professional_number': request.data.get('professionalNumber') or request.data.get('professional_number'),
            'facility_name': request.data.get('facilityName') or request.data.get('facility_name', ''),
            'county': request.data.get('county', ''),
            'sub_county': request.data.get('subCounty') or request.data.get('sub_county'),
            'ward': request.data.get('ward'),
            'malaria_risk_zone': request.data.get('malariaRiskZone', 'HIGH'),
            'pin_hash': request.data.get('pinHash') or request.data.get('pin_hash', '')
        })
        if serializer.is_valid():
            serializer.save()
            return Response({'status': 'created', 'user': serializer.data}, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class AuthLoginView(APIView):
    """API view for user authentication check."""
    def post(self, request):
        identifier = request.data.get('identifier', '')
        pin_hash = request.data.get('pinHash') or request.data.get('pin_hash', '')
        user = UserProfile.objects.filter(phone_number=identifier).first() or UserProfile.objects.filter(email=identifier).first()
        if user:
            serializer = UserProfileSerializer(user)
            return Response({'status': 'authenticated', 'user': serializer.data}, status=status.HTTP_200_OK)
        return Response({'error': 'User not found'}, status=status.HTTP_404_NOT_FOUND)

class BatchSyncView(APIView):
    """API view for manual bulk syncing offline patient data to backend."""
    def post(self, request):
        patients_data = request.data.get('patients', [])
        synced_count = 0
        
        for pdata in patients_data:
            p_uid = pdata.get('patientUid') or pdata.get('patient_uid')
            if not p_uid:
                continue
            patient, _ = Patient.objects.update_or_create(
                patient_uid=p_uid,
                defaults={
                    'full_name': pdata.get('fullName') or pdata.get('full_name', ''),
                    'date_of_birth': pdata.get('dateOfBirth') or pdata.get('date_of_birth', '2024-01-01'),
                    'sex': pdata.get('sex', 'Male'),
                    'caregiver_name': pdata.get('caregiverName') or pdata.get('caregiver_name'),
                    'guardian_phone': pdata.get('guardianPhone') or pdata.get('guardian_phone'),
                    'birth_certificate_number': pdata.get('birthCertificateNumber') or pdata.get('birth_certificate_number'),
                    'facility_name': pdata.get('facilityName') or pdata.get('facility_name', ''),
                    'county': pdata.get('county', ''),
                    'risk_level': pdata.get('riskLevel') or pdata.get('risk_level', 'LOW')
                }
            )
            synced_count += 1

        return Response({
            'status': 'success',
            'syncedCount': synced_count,
            'message': f'Successfully synced {synced_count} patient records'
        }, status=status.HTTP_200_OK)
